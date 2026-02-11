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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    val navigateToShowCard = fun(tree: Tree?) {
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

    // wrong
    //suspend fun getTrees(): List<Tree> {
    //    return dao.getTrees()
    //}
    val getTrees : suspend () -> List<Tree> = {
        dao.getTrees()
    }


    val getTreeByUid: suspend (Int) -> Tree? =
        { uid ->
            dao.findByUid(uid = uid)
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
                            text = "Teaching Mobile 26"
                        )
                    }
                },
                navigationIcon = {
                    Button(
                        modifier = Modifier.semantics { contentDescription = "navigateBack" },
                        onClick = {
                            navController.navigateUp()
                        }) {
                        Text("Back")
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
                    navigateToTreeDisplay = navigateToShowCard
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
                    navigateToSearchFruitsByTree = navigateToSearchFruitsByTree
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
            // EDIT TREE
            composable<SearchFruitsByTreeRoute>{
                    backStackEntry ->
                val tree : SearchFruitsByTreeRoute = backStackEntry.toRoute()
                SearchFruitsByTreeScreen(
                    uidTree = tree.uid,
                    changeMessage = { } ,
                    getFruitsByTreeUid = getFruitsByTreeUid,
                    navigateToFruitDisplay = {}
                )
            }
        }
    }
}