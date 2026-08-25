package com.msa.android.di

import com.msa.android.data.repository.BanksRepositoryImpl
import com.msa.android.data.repository.BullionRepositoryImpl
import com.msa.android.data.repository.ContactRepositoryImpl
import com.msa.android.data.repository.FaqRepositoryImpl
import com.msa.android.data.repository.MetalsRepositoryImpl
import com.msa.android.data.repository.NewsRepositoryImpl
import com.msa.android.data.repository.OnBoardingRepositoryImpl
import com.msa.android.data.repository.PagesRepositoryImpl
import com.msa.android.data.repository.VersionRepositoryImpl
import com.msa.android.domain.repository.BanksRepository
import com.msa.android.domain.repository.BullionRepository
import com.msa.android.domain.repository.ContactRepository
import com.msa.android.domain.repository.FaqRepository
import com.msa.android.domain.repository.MetalsRepository
import com.msa.android.domain.repository.NewsRepository
import com.msa.android.domain.repository.OnBoardingRepository
import com.msa.android.domain.repository.PagesRepository
import com.msa.android.domain.repository.VersionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindMetalsRepository(impl: MetalsRepositoryImpl): MetalsRepository

    @Binds @Singleton
    abstract fun bindBanksRepository(impl: BanksRepositoryImpl): BanksRepository

    @Binds @Singleton
    abstract fun bindFaqRepository(impl: FaqRepositoryImpl): FaqRepository

    @Binds @Singleton
    abstract fun bindPagesRepository(impl: PagesRepositoryImpl): PagesRepository

    @Binds @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds @Singleton
    abstract fun bindOnBoardingRepository(impl: OnBoardingRepositoryImpl): OnBoardingRepository

    @Binds @Singleton
    abstract fun bindVersionRepository(impl: VersionRepositoryImpl): VersionRepository

    @Binds @Singleton
    abstract fun bindBullionRepository(impl: BullionRepositoryImpl): BullionRepository

    @Binds @Singleton
    abstract fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository

    @Binds @Singleton
    abstract fun bindQRRepository(impl: com.msa.android.data.repository.QRRepositoryImpl): com.msa.android.domain.repository.QRRepository

    @Binds @Singleton
    abstract fun bindPortfolioRepository(
        impl: com.msa.android.data.repository.PortfolioRepositoryImpl
    ): com.msa.android.domain.repository.PortfolioRepository
}
