package com.dev.memebattle.core.navigation.layer

import com.dev.memebattle.core.navigation.entry.FeatureEntry
import com.dev.memebattle.core.navigation.route.AppRoute

interface GlobalLayerFeature<R : AppRoute> : FeatureEntry<R>
