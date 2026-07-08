package com.dev.memebattle.feature.home.impl.di

import com.dev.memebattle.feature.home.impl.feature.HomeFeatureEntryImpl


import com.dev.memebattle.core.navigation.entry.FeatureEntry
import org.koin.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homeModule = module {
    
    
    factoryOf(::HomeFeatureEntryImpl) bind FeatureEntry::class
}
