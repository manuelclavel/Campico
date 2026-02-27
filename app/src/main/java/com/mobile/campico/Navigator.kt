package com.mobile.campico

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.graphics.scale
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date
import kotlin.io.encoding.Base64

fun compressImageToLessThan1MB(imageBytes: ByteArray, maxFileSize: Long = 1024000): ByteArray {
    // 1. Decode the original ByteArray into a Bitmap
    var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // 2. Initialize variables for compression
    var quality = 90 // Start with a decent quality
    val outputStream = ByteArrayOutputStream()

    // 3. Compress the bitmap to the output stream
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    var compressedData = outputStream.toByteArray()

    // 4. Iterate to reduce quality or rescale until the size is under the limit
    while (compressedData.size > maxFileSize) {
        outputStream.reset() // Reset the output stream for a new compression attempt
        quality -= 5 // Decrease quality by 5 for the next iteration

        if (quality < 10) {
            // If quality is too low, rescale the image to fewer pixels
            bitmap = bitmap.scale((bitmap.width * 0.8).toInt(), (bitmap.height * 0.8).toInt())
            quality = 90 // Reset quality for the smaller bitmap
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        compressedData = outputStream.toByteArray()
    }

    // 5. Recycle the bitmap to free memory
    bitmap.recycle()

    return compressedData
}

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

    //var code by remember { mutableIntStateOf(0) }
    //var message by remember { mutableStateOf("") }

    var token:String by remember {mutableStateOf("")}
    var email:String by remember {mutableStateOf("")}


    var message by remember { mutableStateOf("") }
    val changeMessage = fun(text: String) {
        message = text
    }

    LaunchedEffect(Unit) {
        val preferencesFlow: Flow<Preferences> = appContext.dataStore.data
        val preferences = preferencesFlow.first()
        token = preferences[TOKEN] ?: ""
        email = preferences[EMAIL] ?: ""
    }
    /* topbar selections */
    var currentVisitUid by remember {mutableStateOf(0)}
    var currentTreeUid by remember {mutableStateOf(0)}

    /* refresh */
    var refreshImagePagerVisit by remember {mutableStateOf(false)}
    val getRefreshImagePagerVisit = fun(): Boolean {
        return refreshImagePagerVisit
    }

    /* camera */
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Log.d("CAMPICO", "successfully capturing image")
                 // Photo captured successfully, imageUri now points to the file
                 // You can initiate the upload here
                 scope.launch {
                     withContext(Dispatchers.IO) {
                         changeMessage("Uploading object in S3...")
                         imageUri?.let { uri ->
                             val byteArray: ByteArray
                             // Use the 'use' extension function to ensure
                             // the InputStream is automatically closed
                             context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                 byteArray = inputStream.readBytes()

                                 /* reduce size */
                                 val compressedByteArray = compressImageToLessThan1MB(
                                     imageBytes = byteArray
                                 )
                                 /* end */
                                 val encodedString = Base64.encode(compressedByteArray)
                                 // generating a unique key
                                 val timestampMillis: Long = System.currentTimeMillis()
                                 val key = "timestamp:$timestampMillis"

                                 // generating a base64 key
                                 val keyAsByteArray = key.toByteArray(Charsets.UTF_8)
                                 val s3key: String = Base64.Default.encode(keyAsByteArray)

                                 val response =
                                     networkService.uploadMediaVisitObject(
                                         upload = UploadMediaVisitRequest(
                                             content = encodedString,
                                             token = token,
                                             email = email,
                                             s3key = s3key,
                                             mediaType = 0,
                                             visitUid = currentVisitUid
                                         )
                                     )
                                 //changeMessage(response.toString())
                                 refreshImagePagerVisit = !(refreshImagePagerVisit)
                             }
                         }

                     }
                 }
            }
        }
    )
    val takePictureVisit = fun (uid:Int){
        val uri = getTmpFileUri(context)
        imageUri = uri
        cameraLauncher.launch(uri)
    }


    /* topbar actions */
    var showTakePictureVisitButton by remember { mutableStateOf(false) }
    var showAddVisitButton by remember { mutableStateOf(false) }
    var showAddTreeButton by remember { mutableStateOf(false) }
    var showAddFruitButton by remember { mutableStateOf(false) }
    var showProfileButton by remember { mutableStateOf(false) }
    var showLogoutButton by remember { mutableStateOf(false) }


    val navigateBack = fun() {
        navController.navigateUp()
    }

    val navigateToHome = fun() {
        navController.navigate(HomeRoute)
    }

    val navigateToProfile = fun() {
        navController.navigate(ProfileRoute)
    }
    val navigateToLogin = fun() {
        navController.navigate(LoginRoute)
    }
    val navigateToToken = fun(email: String) {
        navController.navigate(TokenRoute(email))
    }
    // begin
    val navigateToSearchVisits: () -> Unit = {
        navController.navigate(SearchVisitsRoute)
    }
    val navigateToAddVisit: () -> Unit = {
        navController.navigate(AddVisitRoute)
    }
    /*
    val navigateToEditVisit = fun(visit: Visit?) {
        visit?.uid?.let {
            navController.navigate(
                EditVisitRoute(it)
            )
        }
    }
     */

    // end

    val navigateToSearchTrees: () -> Unit = {
        navController.navigate(SearchTreesRoute)
    }
    val navigateToAddTree: () -> Unit = {
        navController.navigate(AddTreeRoute)
    }
    val navigateToEditTree = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                EditTreeRoute(it)
            )
        }
    }

    val navigateToVisitDisplay = fun(visit: Visit?) {
        visit?.uid?.let {
            navController.navigate(
                ShowVisitRoute(it)
            )
        }
    }


    //val getTreeById: suspend (String) -> Tree? =
    //    { id ->
    //        dao.findTreeById(id = id)
    //    }

    val navigateToTreeDisplayById = fun(id: String) {
        scope.launch {
            val tree = dao.findTreeById(id)
            tree?.uid?.let {
                navController.navigate(
                    ShowTreeRoute(it)
                )
            }
        }
    }
    val navigateToTreeDisplay = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                ShowTreeRoute(it)
            )
        }
    }

    val navigateToFruitDisplayById = fun(id: String) {
        scope.launch {
            val fruit = dao.findFruitById(id)
            fruit?.uid?.let {
                navController.navigate(
                    ShowFruitRoute(it)
                )
            }
        }
    }

    val navigateToSearchFruitsByTree = fun(tree: Tree?) {
        tree?.uid?.let {
            navController.navigate(
                SearchFruitsByTreeRoute(it)
            )
        }
    }
    val navigateToAddFruitByTreeUid = fun(treeUid: Int) {
        navController.navigate(
                AddFruitByTreeRoute(treeUid)
            )
    }

    val navigateToFruitDisplay = fun(fruit: Fruit?) {
        fruit?.uid?.let {
            navController.navigate(
                ShowFruitRoute(it)
            )
        }
    }

    val navigateToEditFruit = fun(fruit: Fruit?) {
        fruit?.uid?.let {
            navController.navigate(
                EditFruitRoute(it)
            )
        }
    }

    val navigateToQRCodeScanner = fun() {
        navController.navigate(QRCodeScannerRoute)
    }

    val navigateToUploadPhoto = fun() {
        navController.navigate(UploadPhotoRoute)
    }

    // wrongchangeMessage
    //suspend fun getTrees(): List<Tree> {
    //    return dao.getTrees()
    //}
    // begin
    val getVisits: suspend () -> List<Visit> = {
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
        { dateOld, dateNew ->
            dao.updateVisit(
                dateOld = dateOld,
                dateNew = dateNew
            )
        }

    // end
    val getTrees: suspend () -> List<Tree> = {
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
        { idOld, idNew ->
            dao.updateTree(
                idOld = idOld,
                idNew = idNew
            )
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
        { idOld, idNew ->
            dao.updateFruit(
                idOld = idOld,
                idNew = idNew
            )
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
                },
                actions = {
                    // Only add the button if the state allows it
                    val currentNavDestination =
                        navController.currentBackStackEntryAsState().value?.destination

                    currentNavDestination?.let {
                        if (it.hasRoute<HomeRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<SearchVisitsRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = true
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<SearchTreesRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = true
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<AddVisitRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<ShowVisitRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = true
                        } else if (it.hasRoute<ShowTreeRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = true
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<ShowFruitRoute>()) {
                            showProfileButton = true
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = false
                            showTakePictureVisitButton = false
                        } else if (it.hasRoute<ProfileRoute>()) {
                            showProfileButton = false
                            showAddVisitButton = false
                            showAddTreeButton = false
                            showAddFruitButton = false
                            showLogoutButton = true
                            showTakePictureVisitButton = false
                        }

                    }

                    val backStackEntry by navController.currentBackStackEntryAsState()
                    when {
                        backStackEntry?.destination?.hasRoute<ShowVisitRoute>() == true -> {
                            currentVisitUid = backStackEntry!!.toRoute<ShowVisitRoute>().uid
                        }
                        backStackEntry?.destination?.hasRoute<ShowTreeRoute>() == true -> {
                            currentTreeUid = backStackEntry!!.toRoute<ShowTreeRoute>().uid
                        }
                    }
                    if (showAddVisitButton) {
                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "navigateToAddVisit" },
                            onClick = {
                                navigateToAddVisit()
                            }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "AddVisitButton"
                            )
                        }
                    }
                    if (showTakePictureVisitButton) {
                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "navigateToTakePictureVisit" },
                            onClick = {
                                takePictureVisit(currentVisitUid)
                            }) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "TakePictureVisitButton"
                            )
                        }
                    }
                    if (showAddTreeButton) {
                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "navigateToAddTree" },
                            onClick = { navigateToAddTree() }
                        ){
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "AddTreeButton"
                            )
                        }
                    }
                    if (showAddFruitButton) {

                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "navigateToAddFruitTree" },
                            onClick =
                                {
                                    navigateToAddFruitByTreeUid(currentTreeUid)
                                }
                         )
                             {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "AddTreeButton"
                            )
                        }
                    }
                    if (showProfileButton) {
                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "navigateToProfile" },
                            onClick = {
                                navigateToProfile()
                            }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "ProfileButton"
                            )
                        }
                    }
                    if (showLogoutButton) {
                        IconButton(
                            modifier = Modifier
                                .semantics { contentDescription = "LogoutButton" },
                            onClick = {
                                scope.launch {
                                    appContext.dataStore.edit { preferences ->
                                        preferences.remove(EMAIL)
                                        preferences.remove(TOKEN)
                                        changeMessage(preferences[EMAIL] ?: "")
                                    }
                                    navigateBack()
                                }
                            }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "ProfileButton"
                            )
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
                    navigateToSearchVisits = navigateToSearchVisits
                )
            }
            // TOKEN
            composable<TokenRoute> { backStackEntry ->
                val tokenRoute: TokenRoute = backStackEntry.toRoute()
                TokenScreen(
                    changeMessage = changeMessage,
                    navigateToHome = navigateToHome,
                    email = tokenRoute.email
                )
            }
            // PROFILE
            // LOGIN
            composable<ProfileRoute> {
                ProfileScreen(
                    changeMessage = changeMessage,
                    navigateToLogin = navigateToLogin,
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
                    networkService = networkService,
                    navigateToVisitDisplay = navigateToVisitDisplay,
                    navigateToAddVisit = navigateToAddVisit
                )
            }
            // ADD VISIT
            composable<AddVisitRoute> {
                AddVisitScreen(
                    changeMessage = changeMessage,
                    networkService = networkService,
                    navigateBack = navigateBack
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
            composable<ShowVisitRoute> { backStackEntry ->
                val visit: ShowVisitRoute = backStackEntry.toRoute()
                ShowVisitScreen(
                    uid = visit.uid,
                    networkService = networkService,
                    changeMessage = changeMessage,
                    navigateBack = navigateBack,
                    getRefreshImagePagerVisit = getRefreshImagePagerVisit
                )
            }
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
                    networkService = networkService,
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
            composable<ShowTreeRoute> { backStackEntry ->
                val tree: ShowTreeRoute = backStackEntry.toRoute()
                ShowTreeScreen(
                    uid = tree.uid,
                    changeMessage = changeMessage,
                    networkService = networkService,
                    navigateToFruitDisplay = navigateToFruitDisplay
                )
            }
            // EDIT TREE
            composable<EditTreeRoute> { backStackEntry ->
                val tree: EditTreeRoute = backStackEntry.toRoute()
                EditTreeScreen(
                    uid = tree.uid,
                    getTreeByUid = getTreeByUid,
                    updateTree = updateTree,
                    changeMessage = changeMessage
                )
            }
            // SEARCH FRUITS BY TREE
            //composable<SearchFruitsByTreeRoute> { backStackEntry ->
            //    val tree: SearchFruitsByTreeRoute = backStackEntry.toRoute()
            //    SearchFruitsByTreeScreen(
            //        uidTree = tree.uid,
            //        changeMessage = changeMessage,
            //        getFruitsByTreeUid = getFruitsByTreeUid,
            //        navigateToFruitDisplay = navigateToFruitDisplay
            //    )
            //}
            // ADD FRUIT BY TREE
            composable<AddFruitByTreeRoute> { backStackEntry ->
                val tree: AddFruitByTreeRoute = backStackEntry.toRoute()

                AddFruitByTreeScreen(
                    treeUid = tree.uid,
                    changeMessage = changeMessage,
                    networkService = networkService,
                    navigateBack = navigateBack
                )
            }
            // SHOW FRUIT
            composable<ShowFruitRoute> { backStackEntry ->
                val fruit: ShowFruitRoute = backStackEntry.toRoute()
                ShowFruitScreen(
                    uid = fruit.uid,
                    //getFruitByUid = getFruitByUid,
                    //deleteFruit = deleteFruit,
                    changeMessage = changeMessage,
                    networkService = networkService,
                    //navigateToEditFruit = navigateToEditFruit,
                    //navigateBack = navigateBack
                )
            }
            // EDIT FRUIT
            composable<EditFruitRoute> { backStackEntry ->
                val fruit: EditFruitRoute = backStackEntry.toRoute()
                EditFruitScreen(
                    uid = fruit.uid,
                    getFruitByUid = getFruitByUid,
                    updateFruit = updateFruit,
                    changeMessage = changeMessage
                )
            }

            // SCANNER
            composable<QRCodeScannerRoute> {
                QRCodeScannerScreen(
                    navigateToTreeDisplayById = navigateToTreeDisplayById,
                    navigateToFruitDisplayById = navigateToFruitDisplayById
                )
            }
        }
    }
}