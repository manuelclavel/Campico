package com.mobile.campico

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object LoginRoute


@Serializable
data class TokenRoute(val email: String)
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

@Serializable
data class AddFruitByTreeRoute(val uid: Int)

@Serializable
data class ShowFruitRoute(val uid: Int)

@Serializable
data class EditFruitRoute(val uid: Int)

@Serializable
object QRCodeScannerRoute