package com.dev.memebattle.feature.gameplay.impl.di

import com.dev.memebattle.feature.gameplay.api.entry.GameplayFeatureEntry
import com.dev.memebattle.feature.gameplay.impl.feature.GameplayFeatureEntryImpl


import com.dev.memebattle.core.navigation.entry.FeatureEntry
import org.koin.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val gameplayModule = module {
    
    
    factoryOf(::GameplayFeatureEntryImpl) bind FeatureEntry::class
}
