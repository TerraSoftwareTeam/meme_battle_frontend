package com.dev.memebattle.feature.packs.impl.presentation.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import com.dev.memebattle.core.navigation.entry.FeatureComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.catalog.PacksCatalogComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.create.PacksCreateComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.details.PacksDetailsComponent
import com.dev.memebattle.feature.packs.impl.presentation.component.edit.PacksEditComponent

sealed interface PacksExtraComponent {
    class Create(val instance: PacksCreateComponent) : PacksExtraComponent
    class Edit(val instance: PacksEditComponent) : PacksExtraComponent
}
interface PacksComponent : FeatureComponent {

    // ── ChildPanels ────────────────────────────────────────────────────────
    @OptIn(ExperimentalDecomposeApi::class)
    val panels: Value<ChildPanels<
            PacksComponentImpl.MainConfig,
            PacksCatalogComponent,
            PacksComponentImpl.DetailsConfig,
            PacksDetailsComponent,
            PacksComponentImpl.ExtraConfig,
            PacksExtraComponent>>

    /**
     * Вызывается из UI для адаптации режима отображения
     * в зависимости от доступного пространства экрана.
     *
     * SINGLE  — телефон (одна панель за раз)
     * DUAL    — планшет / desktop (каталог + правая панель)
     * TRIPLE  — широкий desktop (все три панели)
     */
    fun setAdaptiveMode(mode: ChildPanelsMode)

}
