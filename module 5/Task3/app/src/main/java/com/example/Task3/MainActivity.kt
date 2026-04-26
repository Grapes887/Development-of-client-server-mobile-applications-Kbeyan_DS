package com.example.Task3

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GALLERY_ROUTE = "gallery"
private const val VIEWER_ROUTE = "viewer/{fileName}"
private val photoFileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
private val photoDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"))

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<GalleryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryTheme {
                GalleryApp(viewModel = viewModel)
            }
        }
    }
}

data class PhotoItem(
    val fileName: String,
    val uri: Uri,
    val absolutePath: String,
    val modifiedAtMillis: Long
)

data class GalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<PhotoItem> = emptyList()
)

class GalleryViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        reloadPhotos()
    }

    fun reloadPhotos() {
        viewModelScope.launch {
            val photos = withContext(Dispatchers.IO) {
                val context = getApplication<android.app.Application>()
                val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                directory?.listFiles()
                    .orEmpty()
                    .filter { file -> file.isFile && file.extension.equals("jpg", ignoreCase = true) }
                    .sortedByDescending(File::lastModified)
                    .map { file ->
                        PhotoItem(
                            fileName = file.name,
                            uri = file.toUri(context),
                            absolutePath = file.absolutePath,
                            modifiedAtMillis = file.lastModified()
                        )
                    }
            }
            _uiState.value = GalleryUiState(isLoading = false, photos = photos)
        }
    }

    fun findPhoto(fileName: String?): PhotoItem? {
        return uiState.value.photos.firstOrNull { it.fileName == fileName }
    }
}

@Composable
private fun GalleryApp(viewModel: GalleryViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = GALLERY_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(GALLERY_ROUTE) {
                GalleryScreen(
                    uiState = uiState,
                    onOpenViewer = { fileName ->
                        navController.navigate("viewer/${Uri.encode(fileName)}")
                    },
                    onPhotoCaptured = viewModel::reloadPhotos,
                    snackbarHostState = snackbarHostState
                )
            }
            composable(
                route = VIEWER_ROUTE,
                arguments = listOf(navArgument("fileName") { type = NavType.StringType })
            ) { backStackEntry ->
                val fileName = backStackEntry.arguments?.getString("fileName")?.let(Uri::decode)
                PhotoViewerScreen(
                    photo = viewModel.findPhoto(fileName),
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryScreen(
    uiState: GalleryUiState,
    onOpenViewer: (String) -> Unit,
    onPhotoCaptured: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingExportPhoto by remember { mutableStateOf<PhotoItem?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onPhotoCaptured()
        } else {
            pendingPhotoUri?.let { uri ->
                deletePendingPhoto(context, uri)
            }
            scope.launch {
                snackbarHostState.showSnackbar("Фото не сохранилось. Съёмка была отменена.")
            }
        }
        pendingPhotoUri = null
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val cameraGranted = grants[Manifest.permission.CAMERA] == true
        val writeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            grants[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        }

        if (cameraGranted && writeGranted) {
            val outputUri = createPhotoUri(context)
            pendingPhotoUri = outputUri
            cameraLauncher.launch(outputUri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Без разрешений камера не откроется.")
            }
        }
    }

    val exportPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val photo = pendingExportPhoto
        if (granted && photo != null) {
            scope.launch {
                exportPhoto(context, photo, snackbarHostState)
            }
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Для экспорта на старом Android нужно разрешение на память.")
            }
        }
        pendingExportPhoto = null
    }

    fun launchCamera() {
        val missingPermissions = buildList {
            if (!context.hasPermission(Manifest.permission.CAMERA)) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !context.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (missingPermissions.isEmpty()) {
            val outputUri = createPhotoUri(context)
            pendingPhotoUri = outputUri
            cameraLauncher.launch(outputUri)
        } else {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    fun exportFromGrid(photo: PhotoItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            !context.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            pendingExportPhoto = photo
            exportPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            scope.launch {
                exportPhoto(context, photo, snackbarHostState)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Личная галерея", fontWeight = FontWeight.Bold)
                        Text(
                            "Фото лежат в app-specific папке Pictures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = ::launchCamera,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = "Сделать фото")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE5F3F6), Color(0xFFF7FBFC), Color.White)
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.photos.isEmpty() -> {
                    EmptyPhotoState(onLaunchCamera = ::launchCamera)
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
                    ) {
                        items(uiState.photos, key = { photo -> photo.fileName }) { photo ->
                            PhotoGridItem(
                                photo = photo,
                                onOpenViewer = { onOpenViewer(photo.fileName) },
                                onExport = { exportFromGrid(photo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: PhotoItem,
    onOpenViewer: () -> Unit,
    onExport: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onOpenViewer),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Surface(shape = CircleShape, color = Color(0xBBFFFFFF)) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Меню фото",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Экспорт в галерею") },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoViewerScreen(
    photo: PhotoItem?,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExportPhoto by remember { mutableStateOf<PhotoItem?>(null) }
    val exportPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val target = pendingExportPhoto
        if (granted && target != null) {
            scope.launch {
                exportPhoto(context, target, snackbarHostState)
            }
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Для экспорта на старом Android нужно разрешение на память.")
            }
        }
        pendingExportPhoto = null
    }

    fun exportCurrentPhoto() {
        val target = photo ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            !context.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            pendingExportPhoto = target
            exportPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            scope.launch {
                exportPhoto(context, target, snackbarHostState)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                },
                title = { Text("Просмотр фото") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F8))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (photo == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Фото не найдено", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBack) {
                        Text("Назад")
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = photo.uri),
                        contentDescription = photo.fileName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = photo.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Снято: ${photoDateFormat.format(Date(photo.modifiedAtMillis))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = photo.absolutePath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = ::exportCurrentPhoto,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Экспорт в галерею")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPhotoState(onLaunchCamera: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = Color(0xFFD9EEF2)) {
            Icon(
                imageVector = Icons.Rounded.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.padding(18.dp).size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "У вас пока нет фото",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Сделайте первый снимок и он появится в личной сетке приложения.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        ExtendedFloatingActionButton(
            onClick = onLaunchCamera,
            icon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null) },
            text = { Text("Сделать первое фото") }
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Подгружаю галерею из папки Pictures...")
    }
}

@Composable
private fun GalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun createPhotoUri(context: Context): Uri {
    val outputDir = checkNotNull(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)) {
        "Папка для фото не доступна."
    }
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val fileName = "IMG_${photoFileNameFormat.format(Date())}.jpg"
    val photoFile = File(outputDir, fileName)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

private fun deletePendingPhoto(context: Context, uri: Uri) {
    runCatching {
        File(uri.path.orEmpty()).delete()
    }.recoverCatching {
        context.contentResolver.delete(uri, null, null)
    }
}

private fun File.toUri(context: Context): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        this
    )
}

private suspend fun exportPhoto(
    context: Context,
    photo: PhotoItem,
    snackbarHostState: SnackbarHostState
) {
    val result = withContext(Dispatchers.IO) {
        runCatching { exportPhotoToGallery(context, photo) }
    }

    result.onSuccess {
        snackbarHostState.showSnackbar("Фото добавлено в галерею")
    }.onFailure { error ->
        snackbarHostState.showSnackbar(error.message ?: "Не удалось экспортировать фото")
    }
}

private fun exportPhotoToGallery(context: Context, photo: PhotoItem): Uri {
    val sourceFile = File(photo.absolutePath)
    require(sourceFile.exists()) { "Исходный файл не найден." }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, buildExportName(photo.fileName))
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Module5")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = requireNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ) { "Не удалось создать запись в MediaStore." }

        resolver.openOutputStream(uri)?.use { output ->
            sourceFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: error("Не удалось открыть поток записи для MediaStore.")

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        uri
    } else {
        val targetDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Module5"
        )
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val targetFile = uniqueLegacyExportFile(targetDir, photo.fileName)
        sourceFile.inputStream().use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(targetFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )
        Uri.fromFile(targetFile)
    }
}

private fun buildExportName(originalName: String): String {
    val base = originalName.substringBeforeLast('.', originalName)
    return "${base}_export_${System.currentTimeMillis()}.jpg"
}

private fun uniqueLegacyExportFile(directory: File, originalName: String): File {
    var candidate = File(directory, buildExportName(originalName))
    while (candidate.exists()) {
        candidate = File(directory, buildExportName(originalName))
    }
    return candidate
}
