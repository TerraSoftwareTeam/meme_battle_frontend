package com.dev.memebattle.feature.packs.impl.di

import com.dev.memebattle.feature.packs.api.entry.PacksFeatureEntry
import com.dev.memebattle.feature.packs.impl.feature.PacksFeatureEntryImpl
import com.dev.memebattle.feature.packs.impl.presentation.component.PacksComponentImpl

import com.dev.memebattle.core.navigation.entry.FeatureEntry
import org.koin.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val packsModule = module {
    factoryOf(::PacksComponentImpl)
    factoryOf(::PacksFeatureEntryImpl) bind FeatureEntry::class
}
