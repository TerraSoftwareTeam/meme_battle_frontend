package com.dev.memebattle.feature.gameSetup.impl.di

import com.dev.memebattle.feature.gameSetup.api.entry.GameSetupFeatureEntry
import com.dev.memebattle.feature.gameSetup.impl.feature.GameSetupFeatureEntryImpl


import com.dev.memebattle.core.navigation.entry.FeatureEntry
import org.koin.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val gameSetupModule = module {
    
    
    factoryOf(::GameSetupFeatureEntryImpl) bind FeatureEntry::class
}
