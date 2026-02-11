package com.mobile.campico

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object AddTreeRoute


@Serializable
object SearchTreesRoute

@Serializable
data class ShowTreeRoute(val uid: Int,)

@Serializable
data class EditTreeRoute(val uid: Int)

@Serializable
data class SearchFruitsByTreeRoute(val uid: Int)
