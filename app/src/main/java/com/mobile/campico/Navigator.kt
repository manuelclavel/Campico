package com.mobile.campico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import kotlinx.serialization.serializer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigator(
    navController: NavHostController,
    dao : CampicoDao
) {
    var message by remember { mutableStateOf("") }

    val navigateBack = fun(){
        navController.navigateUp()
    }
    val navigateToSearchTrees : () -> Unit = {
        navController.navigate(SearchTreesRoute)
    }
    val navigateToAddTree : () -> Unit = {
        navController.navigate(AddTreeRoute)
    }
    val navigateToEditTree = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                EditTreeRoute(it))
        }
    }
    val navigateToTreeDisplay = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                ShowTreeRoute(it))
        }
    }

    val navigateToSearchFruitsByTree = fun(tree: Tree?){
        tree?.uid?.let {
            navController.navigate(
                SearchFruitsByTreeRoute(it))
        }
    }
    val navigateToAddFruitByTree = fun(tree: Tree?){
        tree?.uid?.let {
            navController.navigate(
                AddFruitByTreeRoute(it))
        }
    }

    val navigateToFruitDisplay = fun(fruit: Fruit?) {
        fruit?.uid?.let {
            navController.navigate(
                ShowFruitRoute(it))
        }
    }

    val navigateToEditFruit = fun(fruit: Fruit?) {
        fruit?.uid?.let {
            navController.navigate(
                EditFruitRoute(it))
        }
    }

    // wrong
    //suspend fun getTrees(): List<Tree> {
    //    return dao.getTrees()
    //}
    val getTrees : suspend () -> List<Tree> = {
        dao.getTrees()
    }


    val getTreeByUid: suspend (Int) -> Tree? =
        { uid ->
            dao.findTreeByUid(uid = uid)
        }


    val insertTree: suspend (Tree) -> Unit = { tree ->
        dao.insertAll(tree)
    }

    val deleteTree: suspend (String) -> Unit = { id ->
        dao.deleteTree(id = id)
    }

    val updateTree: suspend (String, String) -> Unit =
        { idOld, idNew->
            dao.updateTree(
                idOld = idOld,
                idNew = idNew)
        }

    val getFruitsByTreeUid: suspend (Int) -> List<Fruit> = { uidTree ->
    dao.getFruitsByTreeUid(uidTree = uidTree)
}
    val getTotalFruitsByTreeUid: suspend (Int) -> Int = { uidTree ->
        dao.getTotalFruitsByTreeUid(uidTree = uidTree)
    }

    val insertFruitByTree: suspend (Fruit) -> Unit = { fruit ->
        dao.insertAll(fruit)
    }

    val getFruitByUid: suspend (Int) -> Fruit? =
        { uid ->
            dao.findFruitByUid(uid = uid)
        }

    val deleteFruit: suspend (String) -> Unit = { id ->
        dao.deleteFruit(id = id)
    }

    val updateFruit: suspend (String, String) -> Unit =
        { idOld, idNew->
            dao.updateFruit(
                idOld = idOld,
                idNew = idNew)
        }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "El Campico"
                        )
                    }
                },
                navigationIcon = {
                    val currentRouteIsHome =
                        navController.currentBackStackEntryAsState().value?.destination?.hasRoute<HomeRoute>()
                    if (currentRouteIsHome == false) {
                        Button(
                            modifier = Modifier.semantics { contentDescription = "navigateBack" },
                            onClick = {
                                navController.navigateUp()
                            }) {
                            Text("Back")
                        }
                    }

                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Message"
                            },
                        textAlign = TextAlign.Center,
                        text = message
                    )
                })
        }
    ) { innerPadding ->
        NavHost(
               modifier = Modifier
                   .padding(innerPadding)
                   .fillMaxWidth(),
            navController = navController,
            startDestination = HomeRoute
        ) {
            // HOME
            composable<HomeRoute> {
                HomeScreen(
                    changeMessage = {},
                    navigateToSearchTrees = navigateToSearchTrees,
                    navigateToAddTree = navigateToAddTree
                )
            }
            // SEARCH TREES
            composable<SearchTreesRoute> {
                SearchTreesScreen(
                    changeMessage = {},
                    getTrees = getTrees,
                    navigateToTreeDisplay = navigateToTreeDisplay,
                    getTotalFruitsByTreeUid = getTotalFruitsByTreeUid
                )
            }
            // ADD TREE
            composable<AddTreeRoute> {
                AddTreeScreen(
                    changeMessage = {},
                    insertTree = insertTree
                )
            }
            // SHOW Tree
            composable<ShowTreeRoute>{
                    backStackEntry ->
                val tree : ShowTreeRoute = backStackEntry.toRoute()
                ShowTreeScreen(
                    uid = tree.uid,
                    getTreeByUid = getTreeByUid,
                    deleteTree = deleteTree,
                    changeMessage = {},
                    navigateToEditTree = navigateToEditTree,
                    navigateBack = navigateBack,
                    navigateToSearchFruitsByTree = navigateToSearchFruitsByTree,
                    navigateToAddFruitByTree = navigateToAddFruitByTree
                )
            }
            // EDIT TREE
            composable<EditTreeRoute>{
                    backStackEntry ->
                val tree : EditTreeRoute = backStackEntry.toRoute()
                EditTreeScreen(
                    uid = tree.uid,
                    getTreeByUid = getTreeByUid,
                    updateTree = updateTree,
                    changeMessage = {}
                )
            }
            // SEARCH FRUITS BY TREE
            composable<SearchFruitsByTreeRoute>{
                    backStackEntry ->
                val tree : SearchFruitsByTreeRoute = backStackEntry.toRoute()
                SearchFruitsByTreeScreen(
                    uidTree = tree.uid,
                    changeMessage = { } ,
                    getFruitsByTreeUid = getFruitsByTreeUid,
                    navigateToFruitDisplay = navigateToFruitDisplay
                )
            }
            // ADD FRUIT BY TREE
            composable<AddFruitByTreeRoute>{
                    backStackEntry ->
                val tree : AddFruitByTreeRoute = backStackEntry.toRoute()
                AddFruitByTreeScreen(
                    uidTree = tree.uid,
                    changeMessage = {},
                    insertFruitByTree = insertFruitByTree
                )
            }
            // SHOW Tree
            composable<ShowFruitRoute>{
                    backStackEntry ->
                val fruit : ShowFruitRoute = backStackEntry.toRoute()
                ShowFruitScreen(
                    uid = fruit.uid,
                    getFruitByUid = getFruitByUid,
                    deleteFruit = deleteFruit,
                    changeMessage = {},
                    navigateToEditFruit = navigateToEditFruit,
                    navigateBack = navigateBack
                )
            }
            // EDIT FRUIT
            composable<EditFruitRoute>{
                    backStackEntry ->
                val fruit : EditFruitRoute = backStackEntry.toRoute()
                EditFruitScreen(
                    uid = fruit.uid,
                    getFruitByUid = getFruitByUid,
                    updateFruit = updateFruit,
                    changeMessage = {}
                )
            }
        }
    }
}