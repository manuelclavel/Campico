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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigator(
    navController: NavHostController,
    dao: CampicoDao,
    networkService: NetworkService
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("") }


    val changeMessage = fun(text: String) {
        message = text
    }

    val navigateBack = fun(){
        navController.navigateUp()
    }

    val navigateToHome = fun() {
        navController.navigate(HomeRoute)
    }

    val navigateToLogin = fun() {
        navController.navigate(LoginRoute)
    }
    val navigateToToken = fun(email:String) {
        navController.navigate(TokenRoute(email))
    }
    // begin
    val navigateToSearchVisits : () -> Unit = {
        navController.navigate(SearchVisitsRoute)
    }
    val navigateToAddVisit : () -> Unit = {
        navController.navigate(AddVisitRoute)
    }
    val navigateToEditVisit = fun(visit: Visit?) {
        visit?.uid?.let {
            navController.navigate(
                EditVisitRoute(it))
        }
    }

    // end

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

    val navigateToVisitDisplay = fun(visit: Visit?) {
        visit?.uid?.let {
            navController.navigate(
                ShowVisitRoute(it))
        }
    }


    //val getTreeById: suspend (String) -> Tree? =
    //    { id ->
    //        dao.findTreeById(id = id)
    //    }

    val navigateToTreeDisplayById = fun(id:String) {
        scope.launch {
            val tree = dao.findTreeById(id)
            tree?.uid?.let {
                navController.navigate(
                    ShowTreeRoute(it))
            }
        }
    }
    val navigateToTreeDisplay = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                ShowTreeRoute(it))
        }
    }

    val navigateToFruitDisplayById = fun(id:String) {
        scope.launch {
            val fruit = dao.findFruitById(id)
            fruit?.uid?.let {
                navController.navigate(
                    ShowFruitRoute(it))
            }
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

    val navigateToQRCodeScanner = fun(){
        navController.navigate(QRCodeScannerRoute)
    }

    val navigateToUploadPhoto = fun(){
        navController.navigate(UploadPhotoRoute)
    }

    // wrongchangeMessage
    //suspend fun getTrees(): List<Tree> {
    //    return dao.getTrees()
    //}
    // begin
    val getVisits : suspend () -> List<Visit> = {
        dao.getVisits()
    }

    val getVisitByUid: suspend (Int) -> Visit? =
        { uid ->
            dao.findVisitByUid(uid = uid)
        }

    val insertVisit: suspend (Visit) -> Unit = { visit ->
        dao.insertVisits(visit)
    }

    val deleteVisit: suspend (Date) -> Unit = { date ->
        dao.deleteVisit(date = date)
    }

    val updateVisit: suspend (Date, Date) -> Unit =
        { dateOld, dateNew->
            dao.updateVisit(
                dateOld = dateOld,
                dateNew = dateNew)
        }

    // end
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
                    //val currentRouteIsQRCodeScan =
                    //    navController.currentBackStackEntryAsState().value?.destination?.hasRoute<QRCodeScannerRoute>()
                    //val currentDestination =
                    //    navController.currentBackStackEntryAsState().value?.destination?.toString().orEmpty()
                    if (currentRouteIsHome == false) {
                        Button(
                            modifier = Modifier.semantics { contentDescription = "navigateBack" },
                            onClick = {
                                //if (currentRouteIsQRCodeScan == true){
                                //    navigateToHome()
                                //} else {
                                //    val currentDestination =
                                 //   Log.d("CAMPICO", currentDestination )
                                    navController.navigateUp()
                                //}
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
                    changeMessage = changeMessage,
                    navigateToSearchTrees = navigateToSearchTrees,
                    navigateToAddTree = navigateToAddTree,
                    navigateToLogin = navigateToLogin,
                    networkService = networkService,
                    navigateToQRCodeScanner = navigateToQRCodeScanner,
                    navigateToSearchVisits = navigateToSearchVisits,
                    navigateToAddVisit = navigateToAddVisit,
                    navigateToUploadPhoto = navigateToUploadPhoto
                )
            }
            // TOKEN
            composable<TokenRoute> {
                    backStackEntry ->
                val tokenRoute : TokenRoute = backStackEntry.toRoute()
                TokenScreen(
                    changeMessage = changeMessage ,
                    navigateToHome = navigateToHome,
                    email = tokenRoute.email
                )
            }
            // LOGIN
            composable<LoginRoute> {
                LoginScreen(
                    changeMessage = changeMessage,
                    networkService = networkService,
                    navigateToToken = navigateToToken,
                )
            }
            // SEARCH VISITS
            composable<SearchVisitsRoute> {
                SearchVisitsScreen(
                    changeMessage = changeMessage,
                    getVisits = getVisits,
                    navigateToVisitDisplay = navigateToVisitDisplay
                )
            }
            // ADD VISIT
            composable<AddVisitRoute> {
                AddVisitScreen(
                    changeMessage = changeMessage,
                    insertVisit = insertVisit
                )
            }
            // ADD TREE
            composable<UploadPhotoRoute> {
                UploadPhotoScreen(
                    networkService = networkService,
                    changeMessage = changeMessage
                )
            }
            // SHOW VISIT
           // composable<ShowVisitRoute>{
           //         backStackEntry ->
           //     val visit : ShowVisitRoute = backStackEntry.toRoute()
           //     ShowVisitScreen(
           //         uid = visit.uid,
           //         getVisitByUid = getVisitByUid,
           //         deleteVisit = deleteVisit,
           //         changeMessage = changeMessage,
           //         navigateToEditVist = navigateToEditVisit,
           //         navigateBack = navigateBack
           //     )
           // }
            // EDIT VISIT
          //  composable<EditVisitRoute>{
          //          backStackEntry ->
          //      val visit : EditVisitRoute = backStackEntry.toRoute()
          //      EditVisitScreen(
          //          uid = visit.uid,
          //          getVisitByUid = getVisitByUid,
          //          updateVisit = updateVisit,
          //          changeMessage = changeMessage
           //     )
           // }
            // SEARCH TREES
            composable<SearchTreesRoute> {
                SearchTreesScreen(
                    changeMessage = changeMessage,
                    getTrees = getTrees,
                    navigateToTreeDisplay = navigateToTreeDisplay,
                    getTotalFruitsByTreeUid = getTotalFruitsByTreeUid
                )
            }
            // ADD TREE
            composable<AddTreeRoute> {
                AddTreeScreen(
                    changeMessage = changeMessage,
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
                    changeMessage = changeMessage,
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
                    changeMessage = changeMessage
                )
            }
            // SEARCH FRUITS BY TREE
            composable<SearchFruitsByTreeRoute>{
                    backStackEntry ->
                val tree : SearchFruitsByTreeRoute = backStackEntry.toRoute()
                SearchFruitsByTreeScreen(
                    uidTree = tree.uid,
                    changeMessage = changeMessage,
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
                    changeMessage = changeMessage,
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
                    changeMessage = changeMessage,
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
                    changeMessage = changeMessage
                )
            }
            // SCANNER
            composable<QRCodeScannerRoute>{
                QRCodeScannerScreen(
                    navigateToTreeDisplayById = navigateToTreeDisplayById,
                    navigateToFruitDisplayById = navigateToFruitDisplayById
                )
            }
        }
    }
}