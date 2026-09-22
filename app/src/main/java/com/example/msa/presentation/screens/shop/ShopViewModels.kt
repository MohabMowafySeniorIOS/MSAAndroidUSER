package com.msa.android.presentation.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.data.source.local.UserLocationProvider
import com.msa.android.domain.model.Branch
import com.msa.android.domain.model.Cart
import com.msa.android.domain.model.Order
import com.msa.android.domain.model.PricingMode
import com.msa.android.domain.model.Product
import com.msa.android.domain.model.ProductKind
import com.msa.android.domain.model.ShopInfo
import com.msa.android.domain.model.ShopPrices
import com.msa.android.domain.repository.BranchRepository
import com.msa.android.domain.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/* ═══ المنتجات ═══════════════════════════════════════════════════════ */

data class ProductsState(
    val loading: Boolean = true,
    val products: List<Product> = emptyList(),
    val kind: ProductKind? = null,
    val prices: ShopPrices = ShopPrices(),
    val shop: ShopInfo = ShopInfo(),
    val arabic: Boolean = true,
    val cartCount: Int = 0,
    val failed: Boolean = false,

    /** رسالة قصيرة بتظهر وبتختفي — «تمت الإضافة للسلة» أو سبب الرفض */
    val toast: String? = null
)

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repo: ShopRepository,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsState())
    val state: StateFlow<ProductsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val arabic = language.languageFlow.first() == "ar"

            repo.products(_state.value.kind)
                .onSuccess { (list, prices) ->
                    _state.update {
                        it.copy(
                            loading = false,
                            products = list,
                            prices = prices,
                            arabic = arabic,
                            failed = false
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            arabic = arabic,
                            // القايمة القديمة بتفضل معروضة — الفشل
                            // يبان بس لو مفيش حاجة نعرضها
                            failed = it.products.isEmpty()
                        )
                    }
                }

            // إعدادات المتجر وعدّاد السلة مش حرجين، فالفشل فيهم
            // مبيوقّفش الشاشة
            repo.shopInfo().onSuccess { info -> _state.update { it.copy(shop = info) } }

            refreshCartCount()
        }
    }

    fun setKind(kind: ProductKind?) {
        if (_state.value.kind == kind) return

        _state.update { it.copy(kind = kind) }
        load()
    }

    /**
     * إضافة للسلة.
     *
     * بنحدّث العدّاد من رد السيرفر مش بزيادة محلية: لو السيرفر رفض
     * (مخزون خلص، المتجر مقفول) العدّاد لازم يفضل زي ما هو.
     */
    fun addToCart(product: Product, onNeedsLogin: () -> Unit = {}) {
        viewModelScope.launch {
            repo.addToCart(product.id)
                .onSuccess { cart ->
                    _state.update { it.copy(cartCount = cart.count, toast = "تمت الإضافة للسلة") }
                }
                .onFailure { error ->
                    // 401 معناه المستخدم مش مسجّل — الشاشة بتوديه للدخول
                    if (error.isUnauthorized()) {
                        onNeedsLogin()
                    } else {
                        _state.update { it.copy(toast = error.userMessage()) }
                    }
                }
        }
    }

    fun refreshCartCount() {
        viewModelScope.launch {
            repo.cart().onSuccess { cart -> _state.update { it.copy(cartCount = cart.count) } }
        }
    }

    fun clearToast() = _state.update { it.copy(toast = null) }
}

/* ═══ تفاصيل المنتج ══════════════════════════════════════════════════ */

data class ProductDetailsState(
    val loading: Boolean = true,
    val product: Product? = null,
    val quantity: Int = 1,
    val arabic: Boolean = true,
    val adding: Boolean = false,
    val failed: Boolean = false,
    val toast: String? = null,
    val added: Boolean = false
)

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repo: ShopRepository,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailsState())
    val state: StateFlow<ProductDetailsState> = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val arabic = language.languageFlow.first() == "ar"

            repo.product(id)
                .onSuccess { p -> _state.update { it.copy(loading = false, product = p, arabic = arabic) } }
                .onFailure { _state.update { it.copy(loading = false, arabic = arabic, failed = true) } }
        }
    }

    fun setQuantity(value: Int) {
        val product = _state.value.product ?: return

        // المخزون المفتوح (`null`) سقفه ٩٩ عشان ما يبقاش في رقم
        // مفتوح على الآخر في خانة الكمية
        val max = product.stock ?: 99

        _state.update { it.copy(quantity = value.coerceIn(1, maxOf(1, max))) }
    }

    fun addToCart(onNeedsLogin: () -> Unit = {}) {
        val product = _state.value.product ?: return

        viewModelScope.launch {
            _state.update { it.copy(adding = true) }

            repo.addToCart(product.id, _state.value.quantity)
                .onSuccess { _state.update { it.copy(adding = false, added = true) } }
                .onFailure { error ->
                    _state.update { it.copy(adding = false) }

                    if (error.isUnauthorized()) onNeedsLogin()
                    else _state.update { it.copy(toast = error.userMessage()) }
                }
        }
    }

    fun clearToast() = _state.update { it.copy(toast = null) }
    fun clearAdded() = _state.update { it.copy(added = false) }
}

/* ═══ السلة وإتمام الطلب ═════════════════════════════════════════════ */

data class CartState(
    val loading: Boolean = true,
    val cart: Cart = Cart(),
    val shop: ShopInfo = ShopInfo(),
    val arabic: Boolean = true,

    /** الفروع لاختيار مكان الاستلام — مرتّبة بالأقرب لو الموقع متاح */
    val branches: List<Branch> = emptyList(),
    val selectedBranchId: Long? = null,

    val mode: PricingMode = PricingMode.DEPOSIT,
    val note: String = "",

    val placing: Boolean = false,
    val placedOrder: Order? = null,
    val error: String? = null
) {
    /**
     * الطريقة متاحة لكل المنتجات اللي في السلة؟
     *
     * المشرف بيقدر يقفل «بعربون» على منتج معيّن، والسيرفر بيرفض الطلب
     * كله ساعتها — فالأحسن نخفي الاختيار من الأول بدل ما المستخدم
     * يملا البيانات وياخد رفض.
     */
    val depositAllowed: Boolean
        get() = cart.items.isNotEmpty() && cart.items.all { it.product.allowDeposit }

    val onArrivalAllowed: Boolean
        get() = cart.items.isNotEmpty() && cart.items.all { it.product.allowOnArrival }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: ShopRepository,
    private val branchRepo: BranchRepository,
    private val location: UserLocationProvider,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val arabic = language.languageFlow.first() == "ar"

            repo.cart()
                .onSuccess { cart ->
                    _state.update { it.copy(loading = false, cart = cart, arabic = arabic) }
                    pickDefaultMode()
                }
                .onFailure {
                    _state.update { it.copy(loading = false, arabic = arabic) }
                }

            repo.shopInfo().onSuccess { info -> _state.update { it.copy(shop = info) } }

            loadBranches()
        }
    }

    /**
     * الفروع لاختيار مكان الاستلام.
     *
     * بنبعت الموقع لو متاح عشان السيرفر يرتّب بالأقرب — نفس شاشة
     * الفروع. مش بنطلب الإذن من هنا: لو المستخدم رفضه قبل كده،
     * القايمة بتيجي بترتيب اللوحة وهو يختار بنفسه.
     */
    private fun loadBranches() {
        viewModelScope.launch {
            val point = if (location.hasPermission() && location.isLocationEnabled())
                location.current() else null

            branchRepo.branches(lat = point?.latitude, lng = point?.longitude)
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            branches = list,
                            // أقرب فرع مختار تلقائياً — ده المتوقّع في
                            // أغلب الحالات، والمستخدم يقدر يغيّره
                            selectedBranchId = it.selectedBranchId ?: list.firstOrNull()?.id
                        )
                    }
                }
        }
    }

    /** أول طريقة متاحة بتبقى المختارة — بدل ما الشاشة تفتح على اختيار مرفوض */
    private fun pickDefaultMode() {
        val s = _state.value

        val mode = when {
            s.depositAllowed -> PricingMode.DEPOSIT
            s.onArrivalAllowed -> PricingMode.ON_ARRIVAL
            else -> s.mode
        }

        _state.update { it.copy(mode = mode) }
    }

    fun setQuantity(productId: Long, quantity: Int) {
        viewModelScope.launch {
            repo.setQuantity(productId, quantity)
                .onSuccess { cart -> _state.update { it.copy(cart = cart) } }
                .onFailure { e -> _state.update { it.copy(error = e.userMessage()) } }
        }
    }

    fun remove(productId: Long) {
        viewModelScope.launch {
            repo.removeFromCart(productId).onSuccess { cart ->
                _state.update { it.copy(cart = cart) }
                pickDefaultMode()
            }
        }
    }

    fun setMode(mode: PricingMode) = _state.update { it.copy(mode = mode) }
    fun setBranch(id: Long) = _state.update { it.copy(selectedBranchId = id) }
    fun setNote(value: String) = _state.update { it.copy(note = value) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun placeOrder() {
        val s = _state.value

        if (s.placing || s.cart.isEmpty) return

        viewModelScope.launch {
            _state.update { it.copy(placing = true, error = null) }

            repo.placeOrder(
                mode = s.mode,
                branchId = s.selectedBranchId,
                note = s.note
            )
                .onSuccess { order ->
                    _state.update {
                        // السلة بتتفضّى في السيرفر مع نجاح الطلب،
                        // فبنفضّيها هنا كمان بدل نداء زيادة
                        it.copy(placing = false, placedOrder = order, cart = Cart())
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(placing = false, error = e.userMessage()) }
                }
        }
    }

    fun clearPlacedOrder() = _state.update { it.copy(placedOrder = null) }
}

/* ═══ الطلبات ════════════════════════════════════════════════════════ */

data class OrdersState(
    val loading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val arabic: Boolean = true,
    val failed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repo: ShopRepository,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }

            val arabic = language.languageFlow.first() == "ar"

            repo.orders()
                .onSuccess { list ->
                    _state.update { it.copy(loading = false, orders = list, arabic = arabic) }
                }
                .onFailure {
                    _state.update {
                        it.copy(loading = false, arabic = arabic, failed = it.orders.isEmpty())
                    }
                }
        }
    }

    fun cancel(orderId: Long) {
        viewModelScope.launch {
            repo.cancelOrder(orderId)
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(error = e.userMessage()) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}

/* ── مساعدات الأخطاء ───────────────────────────────────────────────── */

/**
 * رسالة السيرفر لو موجودة، وإلا نص عام.
 *
 * `ShopRequestException` بيشيل رسالة لارافيل بالعربي («السعر مش محدّث
 * دلوقتي فمش ممكن نثبّته»). أي استثناء تاني — شبكة، تحليل — رسالته
 * تقنية ومالهاش معنى للمستخدم، فبنستبدلها.
 */
private fun Throwable.userMessage(): String =
    (this as? com.msa.android.data.repository.ShopRequestException)?.message
        ?: "مش قادرين نكمّل دلوقتي، جرّب تاني"

/**
 * 401 من السيرفر — المستخدم مش مسجّل دخول.
 *
 * بنفحص النوعين: المسارات اللي بترجّع `Response<T>` بيتحوّل فشلها
 * لـ `ShopRequestException` بالكود، واللي بترجّع النوع مباشرةً
 * Retrofit بيرمي `HttpException`.
 */
private fun Throwable.isUnauthorized(): Boolean = when (this) {
    is com.msa.android.data.repository.ShopRequestException -> isUnauthorized
    is retrofit2.HttpException -> code() == 401
    else -> false
}
