package com.msa.android;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.msa.android.data.repository.BanksRepositoryImpl;
import com.msa.android.data.repository.BullionRepositoryImpl;
import com.msa.android.data.repository.ContactRepositoryImpl;
import com.msa.android.data.repository.FaqRepositoryImpl;
import com.msa.android.data.repository.MetalsRepositoryImpl;
import com.msa.android.data.repository.NewsRepositoryImpl;
import com.msa.android.data.repository.OnBoardingRepositoryImpl;
import com.msa.android.data.repository.PagesRepositoryImpl;
import com.msa.android.data.repository.PortfolioRepositoryImpl;
import com.msa.android.data.repository.QRRepositoryImpl;
import com.msa.android.data.repository.VersionRepositoryImpl;
import com.msa.android.data.source.local.BankNotificationPreferences;
import com.msa.android.data.source.local.LanguagePreferences;
import com.msa.android.data.source.local.PortfolioPreferences;
import com.msa.android.data.source.local.PriceDataGenerator;
import com.msa.android.data.source.local.TechnicalAnalysisDataSource;
import com.msa.android.data.source.network.ConnectivityObserver;
import com.msa.android.data.source.network.MsaApi;
import com.msa.android.di.FirebaseModule_ProvideFirestoreFactory;
import com.msa.android.di.LocalPreferencesModule_ProvideDataStoreFactory;
import com.msa.android.di.NetworkModule_ProvideHttpLoggingInterceptorFactory;
import com.msa.android.di.NetworkModule_ProvideMoshiFactory;
import com.msa.android.di.NetworkModule_ProvideMsaApiFactory;
import com.msa.android.di.NetworkModule_ProvideOkHttpClientFactory;
import com.msa.android.di.NetworkModule_ProvideRetrofitFactory;
import com.msa.android.domain.usecase.CalculatePortfolioValue;
import com.msa.android.domain.usecase.GoldValueCalculator;
import com.msa.android.domain.usecase.PriceCalculator;
import com.msa.android.domain.usecase.SilverValueCalculator;
import com.msa.android.domain.usecase.ZakatCalculator;
import com.msa.android.presentation.common.NetworkStatusViewModel;
import com.msa.android.presentation.common.NetworkStatusViewModel_HiltModules;
import com.msa.android.presentation.screens.about.AboutViewModel;
import com.msa.android.presentation.screens.about.AboutViewModel_HiltModules;
import com.msa.android.presentation.screens.appstatus.AppStatusViewModel;
import com.msa.android.presentation.screens.appstatus.AppStatusViewModel_HiltModules;
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel;
import com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel_HiltModules;
import com.msa.android.presentation.screens.banks.BanksViewModel;
import com.msa.android.presentation.screens.banks.BanksViewModel_HiltModules;
import com.msa.android.presentation.screens.calculators.bullions.BullionViewModel;
import com.msa.android.presentation.screens.calculators.bullions.BullionViewModel_HiltModules;
import com.msa.android.presentation.screens.calculators.goldvalue.GoldValueViewModel;
import com.msa.android.presentation.screens.calculators.goldvalue.GoldValueViewModel_HiltModules;
import com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatViewModel;
import com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatViewModel_HiltModules;
import com.msa.android.presentation.screens.calculators.silvervalue.SilverValueViewModel;
import com.msa.android.presentation.screens.calculators.silvervalue.SilverValueViewModel_HiltModules;
import com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatViewModel;
import com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatViewModel_HiltModules;
import com.msa.android.presentation.screens.faq.FaqViewModel;
import com.msa.android.presentation.screens.faq.FaqViewModel_HiltModules;
import com.msa.android.presentation.screens.help.HelpViewModel;
import com.msa.android.presentation.screens.help.HelpViewModel_HiltModules;
import com.msa.android.presentation.screens.home.HomeViewModel;
import com.msa.android.presentation.screens.home.HomeViewModel_HiltModules;
import com.msa.android.presentation.screens.home.banner.BannerApi;
import com.msa.android.presentation.screens.home.banner.BannerModule_ProvideBannerApiFactory;
import com.msa.android.presentation.screens.home.banner.BannerModule_ProvideBannerOkHttpFactory;
import com.msa.android.presentation.screens.home.banner.BannerModule_ProvideBannerRetrofitFactory;
import com.msa.android.presentation.screens.home.banner.BannerRepository;
import com.msa.android.presentation.screens.home.banner.BannerViewModel;
import com.msa.android.presentation.screens.home.banner.BannerViewModel_HiltModules;
import com.msa.android.presentation.screens.indicators.IndicatorsViewModel;
import com.msa.android.presentation.screens.indicators.IndicatorsViewModel_HiltModules;
import com.msa.android.presentation.screens.language.LanguageViewModel;
import com.msa.android.presentation.screens.language.LanguageViewModel_HiltModules;
import com.msa.android.presentation.screens.more.MoreViewModel;
import com.msa.android.presentation.screens.more.MoreViewModel_HiltModules;
import com.msa.android.presentation.screens.news.NewsViewModel;
import com.msa.android.presentation.screens.news.NewsViewModel_HiltModules;
import com.msa.android.presentation.screens.onboarding.OnBoardingViewModel;
import com.msa.android.presentation.screens.onboarding.OnBoardingViewModel_HiltModules;
import com.msa.android.presentation.screens.policy.PolicyViewModel;
import com.msa.android.presentation.screens.policy.PolicyViewModel_HiltModules;
import com.msa.android.presentation.screens.portfolio.AddPortfolioItemViewModel;
import com.msa.android.presentation.screens.portfolio.AddPortfolioItemViewModel_HiltModules;
import com.msa.android.presentation.screens.portfolio.PortfolioViewModel;
import com.msa.android.presentation.screens.portfolio.PortfolioViewModel_HiltModules;
import com.msa.android.presentation.screens.pricegap.PriceGapDetailsViewModel;
import com.msa.android.presentation.screens.pricegap.PriceGapDetailsViewModel_HiltModules;
import com.msa.android.presentation.screens.qr.ScanQRViewModel;
import com.msa.android.presentation.screens.qr.ScanQRViewModel_HiltModules;
import com.msa.android.presentation.screens.splash.SplashViewModel;
import com.msa.android.presentation.screens.splash.SplashViewModel_HiltModules;
import com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisViewModel;
import com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisViewModel_HiltModules;
import com.squareup.moshi.Moshi;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DaggerMSAApplication_HiltComponents_SingletonC {
  private DaggerMSAApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MSAApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MSAApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MSAApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MSAApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MSAApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MSAApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MSAApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MSAApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MSAApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MSAApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MSAApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MSAApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MSAApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(26).put(LazyClassKeyProvider.com_msa_android_presentation_screens_about_AboutViewModel, AboutViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel, AddPortfolioItemViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_appstatus_AppStatusViewModel, AppStatusViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_banks_BanksViewModel, BanksViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_home_banner_BannerViewModel, BannerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_bullions_BullionViewModel, BullionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_faq_FaqViewModel, FaqViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel, GoldValueViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel, GoldZakatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_help_HelpViewModel, HelpViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_indicators_IndicatorsViewModel, IndicatorsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_language_LanguageViewModel, LanguageViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_more_MoreViewModel, MoreViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_common_NetworkStatusViewModel, NetworkStatusViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_news_NewsViewModel, NewsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_onboarding_OnBoardingViewModel, OnBoardingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_policy_PolicyViewModel, PolicyViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_portfolio_PortfolioViewModel, PortfolioViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel, PriceGapDetailsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_qr_ScanQRViewModel, ScanQRViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel, ScreenMaintenanceViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel, SilverValueViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel, SilverZakatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_splash_SplashViewModel, SplashViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel, TechnicalAnalysisViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectLanguagePreferences(instance, singletonCImpl.languagePreferencesProvider.get());
      MainActivity_MembersInjector.injectConnectivityObserver(instance, singletonCImpl.connectivityObserverProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_msa_android_presentation_common_NetworkStatusViewModel = "com.msa.android.presentation.common.NetworkStatusViewModel";

      static String com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel = "com.msa.android.presentation.screens.portfolio.AddPortfolioItemViewModel";

      static String com_msa_android_presentation_screens_news_NewsViewModel = "com.msa.android.presentation.screens.news.NewsViewModel";

      static String com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel = "com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel";

      static String com_msa_android_presentation_screens_onboarding_OnBoardingViewModel = "com.msa.android.presentation.screens.onboarding.OnBoardingViewModel";

      static String com_msa_android_presentation_screens_qr_ScanQRViewModel = "com.msa.android.presentation.screens.qr.ScanQRViewModel";

      static String com_msa_android_presentation_screens_more_MoreViewModel = "com.msa.android.presentation.screens.more.MoreViewModel";

      static String com_msa_android_presentation_screens_banks_BanksViewModel = "com.msa.android.presentation.screens.banks.BanksViewModel";

      static String com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel = "com.msa.android.presentation.screens.calculators.silvervalue.SilverValueViewModel";

      static String com_msa_android_presentation_screens_appstatus_AppStatusViewModel = "com.msa.android.presentation.screens.appstatus.AppStatusViewModel";

      static String com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel = "com.msa.android.presentation.screens.pricegap.PriceGapDetailsViewModel";

      static String com_msa_android_presentation_screens_home_banner_BannerViewModel = "com.msa.android.presentation.screens.home.banner.BannerViewModel";

      static String com_msa_android_presentation_screens_faq_FaqViewModel = "com.msa.android.presentation.screens.faq.FaqViewModel";

      static String com_msa_android_presentation_screens_about_AboutViewModel = "com.msa.android.presentation.screens.about.AboutViewModel";

      static String com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel = "com.msa.android.presentation.screens.calculators.goldvalue.GoldValueViewModel";

      static String com_msa_android_presentation_screens_home_HomeViewModel = "com.msa.android.presentation.screens.home.HomeViewModel";

      static String com_msa_android_presentation_screens_portfolio_PortfolioViewModel = "com.msa.android.presentation.screens.portfolio.PortfolioViewModel";

      static String com_msa_android_presentation_screens_language_LanguageViewModel = "com.msa.android.presentation.screens.language.LanguageViewModel";

      static String com_msa_android_presentation_screens_calculators_bullions_BullionViewModel = "com.msa.android.presentation.screens.calculators.bullions.BullionViewModel";

      static String com_msa_android_presentation_screens_help_HelpViewModel = "com.msa.android.presentation.screens.help.HelpViewModel";

      static String com_msa_android_presentation_screens_indicators_IndicatorsViewModel = "com.msa.android.presentation.screens.indicators.IndicatorsViewModel";

      static String com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel = "com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatViewModel";

      static String com_msa_android_presentation_screens_policy_PolicyViewModel = "com.msa.android.presentation.screens.policy.PolicyViewModel";

      static String com_msa_android_presentation_screens_splash_SplashViewModel = "com.msa.android.presentation.screens.splash.SplashViewModel";

      static String com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel = "com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatViewModel";

      static String com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel = "com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisViewModel";

      @KeepFieldType
      NetworkStatusViewModel com_msa_android_presentation_common_NetworkStatusViewModel2;

      @KeepFieldType
      AddPortfolioItemViewModel com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel2;

      @KeepFieldType
      NewsViewModel com_msa_android_presentation_screens_news_NewsViewModel2;

      @KeepFieldType
      ScreenMaintenanceViewModel com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel2;

      @KeepFieldType
      OnBoardingViewModel com_msa_android_presentation_screens_onboarding_OnBoardingViewModel2;

      @KeepFieldType
      ScanQRViewModel com_msa_android_presentation_screens_qr_ScanQRViewModel2;

      @KeepFieldType
      MoreViewModel com_msa_android_presentation_screens_more_MoreViewModel2;

      @KeepFieldType
      BanksViewModel com_msa_android_presentation_screens_banks_BanksViewModel2;

      @KeepFieldType
      SilverValueViewModel com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel2;

      @KeepFieldType
      AppStatusViewModel com_msa_android_presentation_screens_appstatus_AppStatusViewModel2;

      @KeepFieldType
      PriceGapDetailsViewModel com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel2;

      @KeepFieldType
      BannerViewModel com_msa_android_presentation_screens_home_banner_BannerViewModel2;

      @KeepFieldType
      FaqViewModel com_msa_android_presentation_screens_faq_FaqViewModel2;

      @KeepFieldType
      AboutViewModel com_msa_android_presentation_screens_about_AboutViewModel2;

      @KeepFieldType
      GoldValueViewModel com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel2;

      @KeepFieldType
      HomeViewModel com_msa_android_presentation_screens_home_HomeViewModel2;

      @KeepFieldType
      PortfolioViewModel com_msa_android_presentation_screens_portfolio_PortfolioViewModel2;

      @KeepFieldType
      LanguageViewModel com_msa_android_presentation_screens_language_LanguageViewModel2;

      @KeepFieldType
      BullionViewModel com_msa_android_presentation_screens_calculators_bullions_BullionViewModel2;

      @KeepFieldType
      HelpViewModel com_msa_android_presentation_screens_help_HelpViewModel2;

      @KeepFieldType
      IndicatorsViewModel com_msa_android_presentation_screens_indicators_IndicatorsViewModel2;

      @KeepFieldType
      SilverZakatViewModel com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel2;

      @KeepFieldType
      PolicyViewModel com_msa_android_presentation_screens_policy_PolicyViewModel2;

      @KeepFieldType
      SplashViewModel com_msa_android_presentation_screens_splash_SplashViewModel2;

      @KeepFieldType
      GoldZakatViewModel com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel2;

      @KeepFieldType
      TechnicalAnalysisViewModel com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel2;
    }
  }

  private static final class ViewModelCImpl extends MSAApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AboutViewModel> aboutViewModelProvider;

    private Provider<AddPortfolioItemViewModel> addPortfolioItemViewModelProvider;

    private Provider<AppStatusViewModel> appStatusViewModelProvider;

    private Provider<BanksViewModel> banksViewModelProvider;

    private Provider<BannerViewModel> bannerViewModelProvider;

    private Provider<BullionViewModel> bullionViewModelProvider;

    private Provider<FaqViewModel> faqViewModelProvider;

    private Provider<GoldValueViewModel> goldValueViewModelProvider;

    private Provider<GoldZakatViewModel> goldZakatViewModelProvider;

    private Provider<HelpViewModel> helpViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<IndicatorsViewModel> indicatorsViewModelProvider;

    private Provider<LanguageViewModel> languageViewModelProvider;

    private Provider<MoreViewModel> moreViewModelProvider;

    private Provider<NetworkStatusViewModel> networkStatusViewModelProvider;

    private Provider<NewsViewModel> newsViewModelProvider;

    private Provider<OnBoardingViewModel> onBoardingViewModelProvider;

    private Provider<PolicyViewModel> policyViewModelProvider;

    private Provider<PortfolioViewModel> portfolioViewModelProvider;

    private Provider<PriceGapDetailsViewModel> priceGapDetailsViewModelProvider;

    private Provider<ScanQRViewModel> scanQRViewModelProvider;

    private Provider<ScreenMaintenanceViewModel> screenMaintenanceViewModelProvider;

    private Provider<SilverValueViewModel> silverValueViewModelProvider;

    private Provider<SilverZakatViewModel> silverZakatViewModelProvider;

    private Provider<SplashViewModel> splashViewModelProvider;

    private Provider<TechnicalAnalysisViewModel> technicalAnalysisViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.aboutViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.addPortfolioItemViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.appStatusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.banksViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.bannerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.bullionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.faqViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.goldValueViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.goldZakatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.helpViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.indicatorsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.languageViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.moreViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.networkStatusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.newsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.onBoardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.policyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.portfolioViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.priceGapDetailsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.scanQRViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
      this.screenMaintenanceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 21);
      this.silverValueViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 22);
      this.silverZakatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 23);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 24);
      this.technicalAnalysisViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 25);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(26).put(LazyClassKeyProvider.com_msa_android_presentation_screens_about_AboutViewModel, ((Provider) aboutViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel, ((Provider) addPortfolioItemViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_appstatus_AppStatusViewModel, ((Provider) appStatusViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_banks_BanksViewModel, ((Provider) banksViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_home_banner_BannerViewModel, ((Provider) bannerViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_bullions_BullionViewModel, ((Provider) bullionViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_faq_FaqViewModel, ((Provider) faqViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel, ((Provider) goldValueViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel, ((Provider) goldZakatViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_help_HelpViewModel, ((Provider) helpViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_indicators_IndicatorsViewModel, ((Provider) indicatorsViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_language_LanguageViewModel, ((Provider) languageViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_more_MoreViewModel, ((Provider) moreViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_common_NetworkStatusViewModel, ((Provider) networkStatusViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_news_NewsViewModel, ((Provider) newsViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_onboarding_OnBoardingViewModel, ((Provider) onBoardingViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_policy_PolicyViewModel, ((Provider) policyViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_portfolio_PortfolioViewModel, ((Provider) portfolioViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel, ((Provider) priceGapDetailsViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_qr_ScanQRViewModel, ((Provider) scanQRViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel, ((Provider) screenMaintenanceViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel, ((Provider) silverValueViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel, ((Provider) silverZakatViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_splash_SplashViewModel, ((Provider) splashViewModelProvider)).put(LazyClassKeyProvider.com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel, ((Provider) technicalAnalysisViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_msa_android_presentation_screens_about_AboutViewModel = "com.msa.android.presentation.screens.about.AboutViewModel";

      static String com_msa_android_presentation_screens_calculators_bullions_BullionViewModel = "com.msa.android.presentation.screens.calculators.bullions.BullionViewModel";

      static String com_msa_android_presentation_screens_help_HelpViewModel = "com.msa.android.presentation.screens.help.HelpViewModel";

      static String com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel = "com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisViewModel";

      static String com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel = "com.msa.android.presentation.screens.calculators.goldvalue.GoldValueViewModel";

      static String com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel = "com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatViewModel";

      static String com_msa_android_presentation_screens_indicators_IndicatorsViewModel = "com.msa.android.presentation.screens.indicators.IndicatorsViewModel";

      static String com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel = "com.msa.android.presentation.screens.portfolio.AddPortfolioItemViewModel";

      static String com_msa_android_presentation_screens_more_MoreViewModel = "com.msa.android.presentation.screens.more.MoreViewModel";

      static String com_msa_android_presentation_screens_policy_PolicyViewModel = "com.msa.android.presentation.screens.policy.PolicyViewModel";

      static String com_msa_android_presentation_screens_qr_ScanQRViewModel = "com.msa.android.presentation.screens.qr.ScanQRViewModel";

      static String com_msa_android_presentation_screens_splash_SplashViewModel = "com.msa.android.presentation.screens.splash.SplashViewModel";

      static String com_msa_android_presentation_screens_portfolio_PortfolioViewModel = "com.msa.android.presentation.screens.portfolio.PortfolioViewModel";

      static String com_msa_android_presentation_screens_banks_BanksViewModel = "com.msa.android.presentation.screens.banks.BanksViewModel";

      static String com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel = "com.msa.android.presentation.screens.calculators.silvervalue.SilverValueViewModel";

      static String com_msa_android_presentation_screens_onboarding_OnBoardingViewModel = "com.msa.android.presentation.screens.onboarding.OnBoardingViewModel";

      static String com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel = "com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatViewModel";

      static String com_msa_android_presentation_screens_home_HomeViewModel = "com.msa.android.presentation.screens.home.HomeViewModel";

      static String com_msa_android_presentation_screens_home_banner_BannerViewModel = "com.msa.android.presentation.screens.home.banner.BannerViewModel";

      static String com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel = "com.msa.android.presentation.screens.pricegap.PriceGapDetailsViewModel";

      static String com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel = "com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel";

      static String com_msa_android_presentation_common_NetworkStatusViewModel = "com.msa.android.presentation.common.NetworkStatusViewModel";

      static String com_msa_android_presentation_screens_appstatus_AppStatusViewModel = "com.msa.android.presentation.screens.appstatus.AppStatusViewModel";

      static String com_msa_android_presentation_screens_news_NewsViewModel = "com.msa.android.presentation.screens.news.NewsViewModel";

      static String com_msa_android_presentation_screens_language_LanguageViewModel = "com.msa.android.presentation.screens.language.LanguageViewModel";

      static String com_msa_android_presentation_screens_faq_FaqViewModel = "com.msa.android.presentation.screens.faq.FaqViewModel";

      @KeepFieldType
      AboutViewModel com_msa_android_presentation_screens_about_AboutViewModel2;

      @KeepFieldType
      BullionViewModel com_msa_android_presentation_screens_calculators_bullions_BullionViewModel2;

      @KeepFieldType
      HelpViewModel com_msa_android_presentation_screens_help_HelpViewModel2;

      @KeepFieldType
      TechnicalAnalysisViewModel com_msa_android_presentation_screens_technicalanalysis_TechnicalAnalysisViewModel2;

      @KeepFieldType
      GoldValueViewModel com_msa_android_presentation_screens_calculators_goldvalue_GoldValueViewModel2;

      @KeepFieldType
      GoldZakatViewModel com_msa_android_presentation_screens_calculators_goldzakat_GoldZakatViewModel2;

      @KeepFieldType
      IndicatorsViewModel com_msa_android_presentation_screens_indicators_IndicatorsViewModel2;

      @KeepFieldType
      AddPortfolioItemViewModel com_msa_android_presentation_screens_portfolio_AddPortfolioItemViewModel2;

      @KeepFieldType
      MoreViewModel com_msa_android_presentation_screens_more_MoreViewModel2;

      @KeepFieldType
      PolicyViewModel com_msa_android_presentation_screens_policy_PolicyViewModel2;

      @KeepFieldType
      ScanQRViewModel com_msa_android_presentation_screens_qr_ScanQRViewModel2;

      @KeepFieldType
      SplashViewModel com_msa_android_presentation_screens_splash_SplashViewModel2;

      @KeepFieldType
      PortfolioViewModel com_msa_android_presentation_screens_portfolio_PortfolioViewModel2;

      @KeepFieldType
      BanksViewModel com_msa_android_presentation_screens_banks_BanksViewModel2;

      @KeepFieldType
      SilverValueViewModel com_msa_android_presentation_screens_calculators_silvervalue_SilverValueViewModel2;

      @KeepFieldType
      OnBoardingViewModel com_msa_android_presentation_screens_onboarding_OnBoardingViewModel2;

      @KeepFieldType
      SilverZakatViewModel com_msa_android_presentation_screens_calculators_silverzakat_SilverZakatViewModel2;

      @KeepFieldType
      HomeViewModel com_msa_android_presentation_screens_home_HomeViewModel2;

      @KeepFieldType
      BannerViewModel com_msa_android_presentation_screens_home_banner_BannerViewModel2;

      @KeepFieldType
      PriceGapDetailsViewModel com_msa_android_presentation_screens_pricegap_PriceGapDetailsViewModel2;

      @KeepFieldType
      ScreenMaintenanceViewModel com_msa_android_presentation_screens_appstatus_ScreenMaintenanceViewModel2;

      @KeepFieldType
      NetworkStatusViewModel com_msa_android_presentation_common_NetworkStatusViewModel2;

      @KeepFieldType
      AppStatusViewModel com_msa_android_presentation_screens_appstatus_AppStatusViewModel2;

      @KeepFieldType
      NewsViewModel com_msa_android_presentation_screens_news_NewsViewModel2;

      @KeepFieldType
      LanguageViewModel com_msa_android_presentation_screens_language_LanguageViewModel2;

      @KeepFieldType
      FaqViewModel com_msa_android_presentation_screens_faq_FaqViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.msa.android.presentation.screens.about.AboutViewModel 
          return (T) new AboutViewModel(singletonCImpl.pagesRepositoryImplProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 1: // com.msa.android.presentation.screens.portfolio.AddPortfolioItemViewModel 
          return (T) new AddPortfolioItemViewModel(singletonCImpl.portfolioRepositoryImplProvider.get());

          case 2: // com.msa.android.presentation.screens.appstatus.AppStatusViewModel 
          return (T) new AppStatusViewModel(singletonCImpl.versionRepositoryImplProvider.get());

          case 3: // com.msa.android.presentation.screens.banks.BanksViewModel 
          return (T) new BanksViewModel(singletonCImpl.banksRepositoryImplProvider.get(), singletonCImpl.bankNotificationPreferencesProvider.get());

          case 4: // com.msa.android.presentation.screens.home.banner.BannerViewModel 
          return (T) new BannerViewModel(singletonCImpl.bannerRepositoryProvider.get());

          case 5: // com.msa.android.presentation.screens.calculators.bullions.BullionViewModel 
          return (T) new BullionViewModel(singletonCImpl.bullionRepositoryImplProvider.get(), singletonCImpl.metalsRepositoryImplProvider.get());

          case 6: // com.msa.android.presentation.screens.faq.FaqViewModel 
          return (T) new FaqViewModel(singletonCImpl.faqRepositoryImplProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 7: // com.msa.android.presentation.screens.calculators.goldvalue.GoldValueViewModel 
          return (T) new GoldValueViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.goldValueCalculatorProvider.get());

          case 8: // com.msa.android.presentation.screens.calculators.goldzakat.GoldZakatViewModel 
          return (T) new GoldZakatViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.zakatCalculatorProvider.get());

          case 9: // com.msa.android.presentation.screens.help.HelpViewModel 
          return (T) new HelpViewModel(singletonCImpl.contactRepositoryImplProvider.get());

          case 10: // com.msa.android.presentation.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.banksRepositoryImplProvider.get(), singletonCImpl.priceCalculatorProvider.get());

          case 11: // com.msa.android.presentation.screens.indicators.IndicatorsViewModel 
          return (T) new IndicatorsViewModel(singletonCImpl.priceDataGeneratorProvider.get());

          case 12: // com.msa.android.presentation.screens.language.LanguageViewModel 
          return (T) new LanguageViewModel(singletonCImpl.languagePreferencesProvider.get());

          case 13: // com.msa.android.presentation.screens.more.MoreViewModel 
          return (T) new MoreViewModel(singletonCImpl.versionRepositoryImplProvider.get());

          case 14: // com.msa.android.presentation.common.NetworkStatusViewModel 
          return (T) new NetworkStatusViewModel(singletonCImpl.connectivityObserverProvider.get());

          case 15: // com.msa.android.presentation.screens.news.NewsViewModel 
          return (T) new NewsViewModel(singletonCImpl.newsRepositoryImplProvider.get());

          case 16: // com.msa.android.presentation.screens.onboarding.OnBoardingViewModel 
          return (T) new OnBoardingViewModel(singletonCImpl.onBoardingRepositoryImplProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 17: // com.msa.android.presentation.screens.policy.PolicyViewModel 
          return (T) new PolicyViewModel(singletonCImpl.pagesRepositoryImplProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 18: // com.msa.android.presentation.screens.portfolio.PortfolioViewModel 
          return (T) new PortfolioViewModel(singletonCImpl.portfolioRepositoryImplProvider.get(), singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.priceCalculatorProvider.get(), singletonCImpl.calculatePortfolioValueProvider.get());

          case 19: // com.msa.android.presentation.screens.pricegap.PriceGapDetailsViewModel 
          return (T) new PriceGapDetailsViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.banksRepositoryImplProvider.get());

          case 20: // com.msa.android.presentation.screens.qr.ScanQRViewModel 
          return (T) new ScanQRViewModel(singletonCImpl.qRRepositoryImplProvider.get());

          case 21: // com.msa.android.presentation.screens.appstatus.ScreenMaintenanceViewModel 
          return (T) new ScreenMaintenanceViewModel(singletonCImpl.versionRepositoryImplProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 22: // com.msa.android.presentation.screens.calculators.silvervalue.SilverValueViewModel 
          return (T) new SilverValueViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.silverValueCalculatorProvider.get());

          case 23: // com.msa.android.presentation.screens.calculators.silverzakat.SilverZakatViewModel 
          return (T) new SilverZakatViewModel(singletonCImpl.metalsRepositoryImplProvider.get(), singletonCImpl.zakatCalculatorProvider.get());

          case 24: // com.msa.android.presentation.screens.splash.SplashViewModel 
          return (T) new SplashViewModel();

          case 25: // com.msa.android.presentation.screens.technicalanalysis.TechnicalAnalysisViewModel 
          return (T) new TechnicalAnalysisViewModel(singletonCImpl.technicalAnalysisDataSourceProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MSAApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MSAApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends MSAApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<DataStore<Preferences>> provideDataStoreProvider;

    private Provider<LanguagePreferences> languagePreferencesProvider;

    private Provider<ConnectivityObserver> connectivityObserverProvider;

    private Provider<FirebaseFirestore> provideFirestoreProvider;

    private Provider<PagesRepositoryImpl> pagesRepositoryImplProvider;

    private Provider<PortfolioPreferences> portfolioPreferencesProvider;

    private Provider<PortfolioRepositoryImpl> portfolioRepositoryImplProvider;

    private Provider<VersionRepositoryImpl> versionRepositoryImplProvider;

    private Provider<BanksRepositoryImpl> banksRepositoryImplProvider;

    private Provider<BankNotificationPreferences> bankNotificationPreferencesProvider;

    private Provider<OkHttpClient> provideBannerOkHttpProvider;

    private Provider<Retrofit> provideBannerRetrofitProvider;

    private Provider<BannerApi> provideBannerApiProvider;

    private Provider<BannerRepository> bannerRepositoryProvider;

    private Provider<BullionRepositoryImpl> bullionRepositoryImplProvider;

    private Provider<MetalsRepositoryImpl> metalsRepositoryImplProvider;

    private Provider<FaqRepositoryImpl> faqRepositoryImplProvider;

    private Provider<PriceCalculator> priceCalculatorProvider;

    private Provider<GoldValueCalculator> goldValueCalculatorProvider;

    private Provider<ZakatCalculator> zakatCalculatorProvider;

    private Provider<ContactRepositoryImpl> contactRepositoryImplProvider;

    private Provider<PriceDataGenerator> priceDataGeneratorProvider;

    private Provider<HttpLoggingInterceptor> provideHttpLoggingInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<MsaApi> provideMsaApiProvider;

    private Provider<NewsRepositoryImpl> newsRepositoryImplProvider;

    private Provider<OnBoardingRepositoryImpl> onBoardingRepositoryImplProvider;

    private Provider<CalculatePortfolioValue> calculatePortfolioValueProvider;

    private Provider<QRRepositoryImpl> qRRepositoryImplProvider;

    private Provider<SilverValueCalculator> silverValueCalculatorProvider;

    private Provider<TechnicalAnalysisDataSource> technicalAnalysisDataSourceProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStore<Preferences>>(singletonCImpl, 1));
      this.languagePreferencesProvider = DoubleCheck.provider(new SwitchingProvider<LanguagePreferences>(singletonCImpl, 0));
      this.connectivityObserverProvider = DoubleCheck.provider(new SwitchingProvider<ConnectivityObserver>(singletonCImpl, 2));
      this.provideFirestoreProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseFirestore>(singletonCImpl, 4));
      this.pagesRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PagesRepositoryImpl>(singletonCImpl, 3));
      this.portfolioPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<PortfolioPreferences>(singletonCImpl, 6));
      this.portfolioRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PortfolioRepositoryImpl>(singletonCImpl, 5));
      this.versionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<VersionRepositoryImpl>(singletonCImpl, 7));
      this.banksRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<BanksRepositoryImpl>(singletonCImpl, 8));
      this.bankNotificationPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<BankNotificationPreferences>(singletonCImpl, 9));
      this.provideBannerOkHttpProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 13));
      this.provideBannerRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 12));
      this.provideBannerApiProvider = DoubleCheck.provider(new SwitchingProvider<BannerApi>(singletonCImpl, 11));
      this.bannerRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BannerRepository>(singletonCImpl, 10));
      this.bullionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<BullionRepositoryImpl>(singletonCImpl, 14));
      this.metalsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MetalsRepositoryImpl>(singletonCImpl, 15));
      this.faqRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<FaqRepositoryImpl>(singletonCImpl, 16));
      this.priceCalculatorProvider = DoubleCheck.provider(new SwitchingProvider<PriceCalculator>(singletonCImpl, 18));
      this.goldValueCalculatorProvider = DoubleCheck.provider(new SwitchingProvider<GoldValueCalculator>(singletonCImpl, 17));
      this.zakatCalculatorProvider = DoubleCheck.provider(new SwitchingProvider<ZakatCalculator>(singletonCImpl, 19));
      this.contactRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ContactRepositoryImpl>(singletonCImpl, 20));
      this.priceDataGeneratorProvider = DoubleCheck.provider(new SwitchingProvider<PriceDataGenerator>(singletonCImpl, 21));
      this.provideHttpLoggingInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<HttpLoggingInterceptor>(singletonCImpl, 26));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 25));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 27));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 24));
      this.provideMsaApiProvider = DoubleCheck.provider(new SwitchingProvider<MsaApi>(singletonCImpl, 23));
      this.newsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<NewsRepositoryImpl>(singletonCImpl, 22));
      this.onBoardingRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<OnBoardingRepositoryImpl>(singletonCImpl, 28));
      this.calculatePortfolioValueProvider = DoubleCheck.provider(new SwitchingProvider<CalculatePortfolioValue>(singletonCImpl, 29));
      this.qRRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<QRRepositoryImpl>(singletonCImpl, 30));
      this.silverValueCalculatorProvider = DoubleCheck.provider(new SwitchingProvider<SilverValueCalculator>(singletonCImpl, 31));
      this.technicalAnalysisDataSourceProvider = DoubleCheck.provider(new SwitchingProvider<TechnicalAnalysisDataSource>(singletonCImpl, 32));
    }

    @Override
    public void injectMSAApplication(MSAApplication mSAApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.msa.android.data.source.local.LanguagePreferences 
          return (T) new LanguagePreferences(singletonCImpl.provideDataStoreProvider.get());

          case 1: // androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> 
          return (T) LocalPreferencesModule_ProvideDataStoreFactory.provideDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.msa.android.data.source.network.ConnectivityObserver 
          return (T) new ConnectivityObserver(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.msa.android.data.repository.PagesRepositoryImpl 
          return (T) new PagesRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 4: // com.google.firebase.firestore.FirebaseFirestore 
          return (T) FirebaseModule_ProvideFirestoreFactory.provideFirestore();

          case 5: // com.msa.android.data.repository.PortfolioRepositoryImpl 
          return (T) new PortfolioRepositoryImpl(singletonCImpl.portfolioPreferencesProvider.get());

          case 6: // com.msa.android.data.source.local.PortfolioPreferences 
          return (T) new PortfolioPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.msa.android.data.repository.VersionRepositoryImpl 
          return (T) new VersionRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 8: // com.msa.android.data.repository.BanksRepositoryImpl 
          return (T) new BanksRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 9: // com.msa.android.data.source.local.BankNotificationPreferences 
          return (T) new BankNotificationPreferences(singletonCImpl.provideDataStoreProvider.get());

          case 10: // com.msa.android.presentation.screens.home.banner.BannerRepository 
          return (T) new BannerRepository(singletonCImpl.provideBannerApiProvider.get());

          case 11: // com.msa.android.presentation.screens.home.banner.BannerApi 
          return (T) BannerModule_ProvideBannerApiFactory.provideBannerApi(singletonCImpl.provideBannerRetrofitProvider.get());

          case 12: // @com.msa.android.presentation.screens.home.banner.BannerRetrofit retrofit2.Retrofit 
          return (T) BannerModule_ProvideBannerRetrofitFactory.provideBannerRetrofit(singletonCImpl.provideBannerOkHttpProvider.get());

          case 13: // @com.msa.android.presentation.screens.home.banner.BannerRetrofit okhttp3.OkHttpClient 
          return (T) BannerModule_ProvideBannerOkHttpFactory.provideBannerOkHttp();

          case 14: // com.msa.android.data.repository.BullionRepositoryImpl 
          return (T) new BullionRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 15: // com.msa.android.data.repository.MetalsRepositoryImpl 
          return (T) new MetalsRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 16: // com.msa.android.data.repository.FaqRepositoryImpl 
          return (T) new FaqRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 17: // com.msa.android.domain.usecase.GoldValueCalculator 
          return (T) new GoldValueCalculator(singletonCImpl.priceCalculatorProvider.get());

          case 18: // com.msa.android.domain.usecase.PriceCalculator 
          return (T) new PriceCalculator();

          case 19: // com.msa.android.domain.usecase.ZakatCalculator 
          return (T) new ZakatCalculator();

          case 20: // com.msa.android.data.repository.ContactRepositoryImpl 
          return (T) new ContactRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 21: // com.msa.android.data.source.local.PriceDataGenerator 
          return (T) new PriceDataGenerator();

          case 22: // com.msa.android.data.repository.NewsRepositoryImpl 
          return (T) new NewsRepositoryImpl(singletonCImpl.provideFirestoreProvider.get(), singletonCImpl.provideMsaApiProvider.get(), singletonCImpl.languagePreferencesProvider.get());

          case 23: // com.msa.android.data.source.network.MsaApi 
          return (T) NetworkModule_ProvideMsaApiFactory.provideMsaApi(singletonCImpl.provideRetrofitProvider.get());

          case 24: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 25: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.provideHttpLoggingInterceptorProvider.get());

          case 26: // okhttp3.logging.HttpLoggingInterceptor 
          return (T) NetworkModule_ProvideHttpLoggingInterceptorFactory.provideHttpLoggingInterceptor();

          case 27: // com.squareup.moshi.Moshi 
          return (T) NetworkModule_ProvideMoshiFactory.provideMoshi();

          case 28: // com.msa.android.data.repository.OnBoardingRepositoryImpl 
          return (T) new OnBoardingRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 29: // com.msa.android.domain.usecase.CalculatePortfolioValue 
          return (T) new CalculatePortfolioValue();

          case 30: // com.msa.android.data.repository.QRRepositoryImpl 
          return (T) new QRRepositoryImpl(singletonCImpl.provideFirestoreProvider.get());

          case 31: // com.msa.android.domain.usecase.SilverValueCalculator 
          return (T) new SilverValueCalculator();

          case 32: // com.msa.android.data.source.local.TechnicalAnalysisDataSource 
          return (T) new TechnicalAnalysisDataSource();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
