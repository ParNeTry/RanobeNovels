package com.example.ranobenovels

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.net.URL


class MainActivity : ComponentActivity() {

    lateinit var toastContainer: LinearLayout
    lateinit var parent: LinearLayout
    lateinit var header: LinearLayout

    lateinit var scrollBar: ScrollView
    lateinit var scrollContainer: LinearLayout
    lateinit var chapterShownUI: LinearLayout

    lateinit var centeredBlock: RelativeLayout

    lateinit var picContainer: FrameLayout
    lateinit var picGalleryScrollBar: HorizontalScrollView
    lateinit var picGalleryScrollLayout: LinearLayout
    lateinit var picGalleryScrollContainer: LinearLayout
    lateinit var picGalleryHelpingArrow: ImageView

    lateinit var customScrollBar: FrameLayout
    lateinit var customScrollBarRed: LinearLayout
    lateinit var customScrollBarYellow: LinearLayout
    lateinit var customScrollBarBlue: LinearLayout
    lateinit var customScrollBarCyan: LinearLayout
    lateinit var customScrollBarWhite: LinearLayout
    lateinit var customScrollBarText: TextView

    lateinit var chapterScrollBar: HorizontalScrollView
    lateinit var chapterScrollContainer: LinearLayout
    lateinit var chapterScrollBlock: LinearLayout
    lateinit var chapterUIsvg: ImageView

    lateinit var chapterUIscrollBar: ScrollView
    lateinit var chapterUIscrollContainer: LinearLayout
    lateinit var chapterUIscrollBlock: LinearLayout

    lateinit var ranobeNameScrollBar: HorizontalScrollView
    lateinit var ranobeName: TextView
    var ranobeTitle = ""

    val chScrollContainerArr = mutableListOf<TextView>()
    val chUIscrollContainerArr = mutableListOf<TextView>()

    var isChUIshown = false

    lateinit var closeFw: ImageView
    lateinit var openChUIbtn: ImageView
    lateinit var duckCustomizerBtn: ImageView
    lateinit var delRanobeBtn: ImageView
    lateinit var nextBtn: ImageView
    lateinit var backBtn: ImageView
    lateinit var toBmBtn: ImageView
    lateinit var swapThemeBtn: ImageView

    var duckLayout = mutableListOf<LinearLayout>() // ниже приведен макет утки для правильной работы с материалами
    var duckArr = mutableListOf<ImageView>() // книжная закладка утка

    val paragraphArr = mutableListOf<TextView>() // целые абзацы в главе
    var paragraphCount = 0
    var paragraphIndex = 0
    lateinit var paragraphBuffer: Job
    lateinit var chJob: Job
    lateinit var chUIjob: Job

    var imageArr = mutableListOf<ImageView>() // предварительный просмотр определенной главы
    var imagesArr = mutableListOf<ImageView>() // изображения определенной главы
    lateinit var picBuffer: Job

    lateinit var customScrBbuffer: Job

    var scrollPos = 0
    var chScrollPos = 0
    var chUIscrollPos = 0
    var selectedChH = 0
    // locals that moved to globals for some reasons
    var chUImidSvg = listOf<Int>(
        R.drawable._f302,R.drawable._602,R.drawable._614,R.drawable._f327,
        R.drawable._615,R.drawable._f9cb,R.drawable._f37f,R.drawable._f369,
        R.drawable._f30c,R.drawable._f303,R.drawable._f309,R.drawable._f306
    )
    var c = 1
    var of = 0
    var oldScr = 0
    var oldOf = 0
    //

    var bookMarked = false

    var lightTheme = false

    var delPressCount = 5 // прежде чем удалить главы из приложения, вы нажимаете 5 раз
    var errPressCount = 5 // прежде чем дать совет по применению, он сам пробует 5 раз

    var appFolderName = "RanobeNovels"

    var bMdataFileName = "storedData.txt"
    var storedBmDataArr = mutableListOf<String>()

    var chDataFileName = "chStoredData.txt"
    var storedChDataArr = mutableListOf<String>()

    var ranobeTitleFileName = "ranobeTitle.txt"

    lateinit var inputURL : URL
    lateinit var nextChapterFromHTML : String
    var wrongInputURL  = false // отправить, если ссылка недействительна
    var successConnect = false // отправить, когда ссылка будет действительна
    var parsingError = false // отправляйте, когда по какой-то причине не удается загрузить главы
    var doesLastChapterDownloaded = false // отправить, когда последняя глава будет разобрана и сохранена
    var isDownloading = false // отправить, если не удается, нажмите enter, чтобы загрузить главы из ranobelib
    var chaptersAmount = 0

    var chFolderName = "Chapters"
    var chIndex = 0
    var chText = mutableListOf<String>() // содержание абзацев
    var chNumArr = mutableListOf<String>() // имена всех файлов глав

    var picFolderName = ".Pictures"

    val STORAGE_PERMISSION_CODE = 100
    val INTERNET_PERMISSION_CODE = 101
    val NOTIFICATIONS_PERMISSION_CODE = 102

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "channelID"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("RestrictedApi", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // создает любой пользовательский интерфейс при запуске функции onCreate()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //

        // поиск идентификаторов основных блоков и контейнеров
        toastContainer = findViewById(R.id._toastContainer)
        parent = findViewById(R.id._parent)
        header = findViewById(R.id._header)
        centeredBlock = findViewById(R.id._centered_block)
        ranobeName = findViewById(R.id._ranobeName)
        ranobeNameScrollBar = findViewById(R.id._ranobeNameScrollBar)
        chapterShownUI = findViewById(R.id._chapterShownUI)
        picContainer = findViewById(R.id._picture_container)
        picGalleryScrollLayout = findViewById(R.id._picture_gallery_scroll_layout)
        picGalleryScrollBar = findViewById(R.id._picture_gallery_scroll_bar)
        picGalleryScrollContainer = findViewById(R.id._picture_gallery_scroll_container)
        picGalleryHelpingArrow = findViewById(R.id._picture_gallery_helping_arrow)
        customScrollBar = findViewById(R.id._customScrollBar)
        customScrollBarRed = findViewById(R.id._customScrollBarRed)
        customScrollBarYellow = findViewById(R.id._customScrollBarYellow)
        customScrollBarBlue = findViewById(R.id._customScrollBarBlue)
        customScrollBarCyan = findViewById(R.id._customScrollBarCyan)
        customScrollBarWhite = findViewById(R.id._customScrollBarWhite)
        customScrollBarText = findViewById(R.id._customScrollBarText)
        scrollContainer = findViewById(R.id._scrollContainer)
        scrollBar = findViewById(R.id._scrollBar)
        chapterScrollBar = findViewById(R.id._chapterScrollBar)
        chapterScrollContainer = findViewById(R.id._chapterScrollContainer)
        chapterScrollBlock = findViewById(R.id._chapterScrollBlock)
        chapterUIscrollBar = findViewById(R.id._chapterUIscrollBar)
        chapterUIscrollContainer = findViewById(R.id._chapterUIscrollContainer)
        chapterUIsvg = findViewById(R.id._chapter_UI_svg)
        chapterUIscrollBlock = findViewById(R.id._chapterUIscrollBlock)
        openChUIbtn = findViewById(R.id._toCurrentChapter)
        duckCustomizerBtn = findViewById(R.id._duckCustomizerBtn)
        delRanobeBtn = findViewById(R.id._delRanobeBtn)
        nextBtn = findViewById(R.id._next)
        backBtn = findViewById(R.id._back)
        toBmBtn = findViewById(R.id._toBookmark)
        swapThemeBtn = findViewById(R.id._swapTheme)
        //

        ranobeNameScrollBar.setOnTouchListener { v, event ->
            true
        }

        CreateNotificationChannel()

        CheckPermission()

        if (!CheckPermission()) {
            AllowPermissionBlock()
            return // возвращает это значение, поскольку для приведенного ниже кода требуются все эти разрешения, указанные выше
        }

        // создает папки приложений и файлы данных
        CreateContentStorage()
        //

        // поиск файлов глав в определенной папке добавляет их названия в chNumArr[] и сортирует их внутри от !min до max!
        ChNumsSearchAndSort()
        //

        // если предыдущая прога не нашла внутри ничего интересного, сгенеририруется 'приветственный контейнер'
        if (chNumArr.isEmpty()) {
            WelcomeViewBlock()
            return // возвращает это, потому что приведенный ниже код может быть выполнен только в том случае, если у пользователя есть одна или несколько глав в определенной папке
        }
        //
        swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
        //
        // 'перейти к текущей главе в меню' это действие кнопки при нажатии
        openChUIbtn.setOnClickListener {

            isChUIshown = !isChUIshown

            if (!isChUIshown) {
                openChUIbtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_from_180_fa))
                delRanobeBtn.visibility = View.GONE
                chapterUIsvg.visibility = View.GONE
                duckCustomizerBtn.visibility = View.VISIBLE

                chapterScrollBar.visibility = View.VISIBLE

                scrollContainer.animate().alpha(1f).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()

                picContainer.animate().alpha(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
            } else {
                openChUIbtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_to_180_fa))
                delRanobeBtn.visibility = View.VISIBLE
                duckCustomizerBtn.visibility = View.GONE
                chapterScrollBar.visibility = View.GONE
                chapterUIsvg.visibility = View.VISIBLE


                scrollContainer.animate().alpha(0.4f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                picContainer.animate().alpha(0.4f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                chapterShownUI.animate().translationY(0f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
        //
        // 'изменение визуальных элементов закладок' это действие кнопки при нажатии
        duckCustomizerBtn.setOnClickListener {
            duckCustomizerBtn.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.rotate_like_rotor
                )
            )
            coolToast(R.drawable.open_gallery, "Данная кнопка позволяет настроить тему закладке, но пока что тут ничего нет, скоро давлю.", 10000)
        }
        //
        // 'удалить главы ранобэ' это действие кнопки при нажатии
        delRanobeBtn.setOnClickListener {

            delRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            delPressCount--

            if (delPressCount != 0) {
                coolToast(R.drawable.trash,"Нажмите ещё $delPressCount раз, чтобы удалить все главы")
            } else {

                delPressCount = 5
                customScrBbuffer.cancel()
                paragraphBuffer.cancel()
                picBuffer.cancel()
                
                chJob.cancel()
                chUIjob.cancel()

                WelcomeViewBlock()

                DeleteChapters()

                ranobeTitle = ""

                coolToast(R.drawable.duck_customizer,"Удаление успешно")
            }
        }
        //
        // действие кнопки "вернуться к готовому контенту" при нажатии
        toBmBtn.setOnClickListener {
            toBmBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_like_rotor))
            scrollBar.smoothScrollTo(0, scrollPos)
            chapterScrollBar.smoothScrollTo(chScrollPos, 0)
            if(isChUIshown) {
                chapterUIscrollBar.smoothScrollTo(0, chUIscrollPos)
            }
            if (scrollBar.scrollY == scrollPos && chapterScrollBar.scrollX == chScrollPos) {
                coolToast(R.drawable.tobookmark,"В прошлый раз вы остановились в этом месте.")
            }
            else CheckScrollPos(lifecycleScope)
        }
        //
        // действие кнопки "поменять тему" при нажатии
        swapThemeBtn.setOnClickListener {

            lightTheme = !lightTheme

            swapThemeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_360))

            if (!lightTheme) {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.dark_theme))
            } else {
                swapThemeBtn.setImageDrawable(getDrawable(R.drawable.light_theme))
            }

                coolToast(R.drawable.dark_theme, "В данный момент сменить тему нельзя, но будет автоматически меняться с темой телефона.")
        }
        //
        // действие кнопки "следующая глава" при нажатии
        nextBtn.setOnClickListener {

            if (chIndex == chNumArr.lastIndex || chIndex == chNumArr.lastIndex) {
                val animator = ValueAnimator.ofFloat(0f, -20f, 0f).apply {
                    addUpdateListener { animation ->
                        nextBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                coolToast(R.drawable.duck_customizer,"Вы находитесь на последней главе.")
                return@setOnClickListener
            }

            customScrBbuffer.cancel()
            paragraphBuffer.cancel()
            picBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            chIndex += 1

            nextBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chScrollPos = chScrollContainerArr[chIndex].left
            chUIscrollPos = chUIscrollContainerArr[chIndex].top - selectedChH

            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)
            SaveData()
            ClearReadingBlock()
            EraseBookMarkData()

            ReadingBlock(lifecycleScope)

        }
        //
        // действие кнопки "предыдущая глава" при нажатии
        backBtn.setOnClickListener {

            if (chIndex == 0) {
                val animator = ValueAnimator.ofFloat(0f, 20f, 0f).apply {
                    addUpdateListener { animation ->
                        backBtn.translationX = animation.animatedValue as Float
                    }
                    duration = 250
                    start()
                }
                coolToast(R.drawable.duck_customizer,"Вы находитесь на предыдущей главе.")
                return@setOnClickListener
            }

            customScrBbuffer.cancel()
            paragraphBuffer.cancel()
            picBuffer.cancel()
            chJob.cancel()
            chUIjob.cancel()

            chIndex -= 1

            backBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))

            scrollBar.scrollTo(0, 0)

            chScrollPos = chScrollContainerArr[chIndex].left
            chUIscrollPos = chUIscrollContainerArr[chIndex].top - selectedChH

            chScrollContainerArr.clear()
            chapterScrollContainer.removeAllViews()
            chUIscrollContainerArr.clear()
            chapterUIscrollContainer.removeAllViews()
            ChaptersSelectBlock(lifecycleScope)

            SaveData()

            ClearReadingBlock()
            EraseBookMarkData()

            ReadingBlock(lifecycleScope)

        }
        //
        try {
            // генерирует блок с абзацами при запуске функции onCreate()
            ReadingBlock(lifecycleScope)
            //

            // генерирует блок с выбором глав
            ChaptersSelectBlock(lifecycleScope)
            //

            val titleAnim = lifecycleScope.launch {
                while(true) {
                    val time: Long = 25000
                    while(true) {
                        ranobeName.translationX = resources.displayMetrics.widthPixels.toFloat()
                        ranobeName.animate().translationX(-resources.displayMetrics.widthPixels-getViewWidth(ranobeName).toFloat()).setDuration(time).setInterpolator(LinearInterpolator()).start()
                        delay(time)
                    }
                }
            }

        } catch (e: Exception) {

            // сделайте все нулевым
            EraseBookMarkData()
            EraseChData()
            //

            // сохраняет их
            SaveData()
            //

            // попробуйте сгенерировать еще раз
            ReadingBlock(lifecycleScope)
            ChaptersSelectBlock(lifecycleScope)
            //

            val titleAnim = lifecycleScope.launch {
                while(true) {
                    val time: Long = 25000
                    while(true) {
                        ranobeName.translationX = resources.displayMetrics.widthPixels.toFloat()
                        ranobeName.animate().translationX(-resources.displayMetrics.widthPixels-getViewWidth(ranobeName).toFloat()).setDuration(time).setInterpolator(LinearInterpolator()).start()
                        delay(time)
                    }
                }
            }
        }
    }
    //
    @SuppressLint("SuspiciousIndentation")
    override fun onStop() {
        super.onStop()
        if(isDownloading)
        ShowNotification("Приложение свёрнуто, чего ждать?", "Загрузка происходит в фоновом режиме.")
    }
    // Блок с содержанием главы
    @SuppressLint("ClickableViewAccessibility")
    fun ReadingBlock(scope: CoroutineScope) {

        centeredBlock.visibility = View.GONE

        LoadBookMarkData()
        LoadChapterData()

        // novel name
        ranobeName.text = ranobeTitle
        //

        paragraphCount = chText.lastIndex

        for (i in 0..paragraphCount) {
            paragraphArr += TextView(this)
            duckLayout += LinearLayout(this)
            duckArr += ImageView(this)
        }

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val picFolder = File(appFolder, picFolderName)

        // создает одиночную картинку в качестве предварительного просмотра главы?
        picBuffer = scope.launch {
            if(File(picFolder, "${chNumArr[chIndex].substringBefore(".txt")}").exists()) {

                val chPicFolder = File(picFolder, "${chNumArr[chIndex].substringBefore(".txt")}")

                for (i in 0..chPicFolder.listFiles().size) {
                    imageArr += ImageView(applicationContext)
                    imagesArr += ImageView(applicationContext)
                }

                for (pics in chPicFolder.listFiles()) {

                    val img = imagesArr[chPicFolder.listFiles().indexOf(pics)]

                    val bitmap = BitmapFactory.decodeFile(pics.absolutePath)
                    val roBitmap = ImageHelper.getRoundedCornerBitmap(bitmap, dpToFloat(30).toInt())

                    var bmW = roBitmap.width
                    var bmH = roBitmap.height

                    while(bmW > dpToFloat(370).toInt()) {
                        bmW = (bmW/1.1).toInt()
                        bmH = (bmH/1.1).toInt()
                    }
                    while(bmW < dpToFloat(360).toInt()) {
                        bmW = (bmW*1.1).toInt()
                        bmH = (bmH*1.1).toInt()
                    }

                    img.setImageBitmap(Bitmap.createScaledBitmap(roBitmap, bmW, bmH, false))
                    if(imagesArr.size > 2) {

                        img.setPadding(
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt(),
                            dpToFloat(10).toInt()
                        )
                    }
                    else {
                        img.setPadding(
                            (resources.displayMetrics.widthPixels - getViewWidth(img))/2,
                            0,
                            (resources.displayMetrics.widthPixels - getViewWidth(img))/2,
                            0
                        )
                    }

                    picGalleryScrollContainer.addView(img)

                    img.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        customScrollBar.visibility = View.VISIBLE
                        scrollBar.visibility = View.VISIBLE
                        header.visibility = View.VISIBLE
                        chapterShownUI.visibility = View.VISIBLE

                        picGalleryScrollLayout.visibility = View.GONE

                        val job = lifecycleScope.launch {
                            delay(1)
                            picGalleryScrollBar.scrollTo(dpToFloat(0).toInt(),0)
                        }

                        for(v in imageArr) {
                            v.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))
                        }
                    }
                }

                // создает картинку в качестве предварительного просмотра главы
                for (pic in chPicFolder.listFiles()) {

                    val img = imageArr[chPicFolder.listFiles().indexOf(pic)]

                    val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
                    val roBitmap = ImageHelper.getRoundedCornerBitmap(bitmap, dpToFloat(30)
                        .toInt())

                    var bmW = roBitmap.width
                    var bmH = roBitmap.height

                    while(bmW > dpToFloat(230).toInt()) {
                        bmW = (bmW/1.1).toInt()
                        bmH = (bmH/1.1).toInt()
                    }
                    while(bmW < dpToFloat(230).toInt()) {
                        bmW = (bmW*1.1).toInt()
                        bmH = (bmH*1.1).toInt()
                    }

                    img.setImageBitmap(Bitmap.createScaledBitmap(roBitmap, bmW, bmH, false))

                    img.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    img.setPadding(
                        dpToFloat(0).toInt(),
                        dpToFloat(5).toInt(),
                        dpToFloat(0).toInt(),
                        dpToFloat(30).toInt())
                    img.foregroundGravity = Gravity.CENTER
                    img.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                    picContainer.addView(img)

                    val dot = ImageView(applicationContext)
                    dot.setImageResource(R.drawable.open_gallery)
                    val animatable = dot.drawable as Animatable
                    animatable.start()

                    dot.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    dot.setPadding(
                        0,
                        getViewHeight(img) - dpToFloat(30).toInt() - getViewHeight(dot)/2,
                        0,
                        0
                    )
                    dot.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                    img.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        customScrollBar.visibility = View.GONE
                        scrollBar.visibility = View.GONE
                        header.visibility = View.GONE
                        chapterShownUI.visibility = View.GONE

                        picGalleryScrollLayout.visibility = View.VISIBLE
                        picGalleryScrollContainer.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.simple_appearing))

                        val job = lifecycleScope.launch {

                            if(imagesArr.size > 2) {
                                picGalleryHelpingArrow.visibility = View.VISIBLE
                                picGalleryHelpingArrow.animation = AnimationUtils.loadAnimation(applicationContext, R.anim.helping_arrow)
                            }
                            else picGalleryHelpingArrow.visibility = View.GONE
                            delay(10)
                            picGalleryScrollBar.smoothScrollTo(dpToFloat(60).toInt(),0)
                            delay(500)
                            picGalleryScrollBar.smoothScrollTo(dpToFloat(0).toInt(),0)
                        }
                    }
                    // возвращает процесс, если фотографий в папке главы находится болеше 1
                    if(chPicFolder.listFiles().size > 1) {
                        picContainer.addView(dot)
                        return@launch
                    }
                    //
                }
                //
            }
        }
        //

        // блок для создания пользовательского интерфейса с изображениями глав
        // ...
        //

        paragraphBuffer = scope.launch {
            var posOff = 0
            for (paragraph in paragraphArr) {

                // возврат полосы прокрутки к нижней горизонтальной строке раздела
                if(posOff != 10) {
                    chapterScrollBar.scrollTo(chScrollPos, 0)
                    posOff += 1
                }
                //

                if (paragraphArr.indexOf(paragraph) < paragraphArr.size) {

                    var k = paragraphArr.indexOf(paragraph)

                    // paragraph
                    paragraph.text = chText[k]
                    paragraph.setTextColor(getColor(R.color._textColor))
                    paragraph.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    if (paragraphArr.indexOf(paragraph) != paragraphArr.lastIndex)
                        paragraph.setPadding(
                            dpToFloat(30).toInt(), 0, dpToFloat(30).toInt(), 0)
                    else
                        paragraph.setPadding(
                            dpToFloat(30).toInt(),
                            0,
                            dpToFloat(30).toInt(),
                            dpToFloat(60).toInt()
                        )

                    scrollContainer.addView(paragraph)

                    var animation = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.simple_appearing
                    )
                    paragraph.animation = animation
                    //

                    var duckLayoutId = duckLayout[paragraphArr.indexOf(paragraph)]
                    duckLayoutId.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    duckLayoutId.gravity = Gravity.LEFT

                    // закладка "утка"
                    var duckId = duckArr[paragraphArr.indexOf(paragraph)]

                    duckId.setImageDrawable(getDrawable(R.drawable.duckbookmark))
                    duckId.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    duckId.scaleX = 1.3f
                    duckId.scaleY = 1.3f
                    duckId.setPadding(0, dpToFloat(10).toInt(), 0, dpToFloat(10)
                        .toInt())
                    duckId.visibility = View.INVISIBLE
                    duckId.foregroundGravity = Gravity.LEFT
                    //

                    // нужно добавить в duckLayout вид изображения duck, а затем добавить его в контейнер для прокрутки
                    duckLayoutId.addView(duckId)
                    scrollContainer.addView(duckLayoutId)
                    //

                    paragraph.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        if (paragraph.text.toString().contains("*")) {

                            bookMarked = false

                            if (!bookMarked) {
                                for (i in 0..paragraphArr.lastIndex) {
                                    if (duckArr[i].visibility == View.VISIBLE) {
                                        duckArr[i].animation = AnimationUtils.loadAnimation(
                                            applicationContext,
                                            R.anim.duck_out
                                        )
                                        Handler().postDelayed({
                                            duckArr[i].visibility = View.INVISIBLE
                                        }, 300)
                                    }
                                    duckArr[i].visibility = View.INVISIBLE
                                }
                                TextColorNormalize()
                            }

                            paragraphIndex = paragraphArr.indexOf(paragraph) - 1
                            bookMarked = paragraphIndex > 0
                            scrollPos = scrollBar.scrollY

                            SaveData()

                            toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))

                            scrollBar.smoothScrollTo(0, scrollContainer.height)

                            for (i in 0..paragraphArr.lastIndex) {
                                paragraphArr[i].setTextColor(getColor(R.color._textColor))
                            }
                            for (i in 0..paragraphIndex) {
                                if (paragraphIndex > 0)
                                    paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                            }
                        }
                    }

                    // активация кнопки "утиная закладка" при нажатии
                    duckLayoutId.setOnClickListener {

                        if(isChUIshown) {
                            ClearChSelBlock()
                            return@setOnClickListener
                        }

                        bookMarked = !bookMarked
                        paragraphIndex = paragraphArr.indexOf(paragraph)
                        scrollPos = scrollBar.scrollY

                        SaveData()

                        if (!bookMarked) {
                            for (i in 0..paragraphArr.lastIndex) {
                                if (duckArr[i].visibility == View.VISIBLE) {
                                    duckArr[i].animation = AnimationUtils.loadAnimation(
                                        applicationContext,
                                        R.anim.duck_out
                                    )
                                    Handler().postDelayed({
                                        duckArr[i].visibility = View.INVISIBLE
                                    }, 300)
                                }
                                duckArr[i].visibility = View.INVISIBLE
                            }
                            toBmBtn.setColorFilter(getColor(R.color._sideColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                            TextColorNormalize()
                        } else {
                            for (i in 0..paragraphArr.lastIndex) {
                                if (i == paragraphIndex) duckArr[i].visibility = View.VISIBLE
                                else duckArr[i].visibility = View.INVISIBLE
                            }
                            duckId.animation =
                                AnimationUtils.loadAnimation(applicationContext, R.anim.duck_in)
                            for (i in 0..paragraphIndex) {
                                paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                            }
                            toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                            toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                        }
                    }
                    if (!bookMarked) {
                        for (i in 0..paragraphArr.lastIndex) {
                            duckArr[i].visibility = View.INVISIBLE
                        }
                        toBmBtn.setColorFilter(getColor(R.color._sideColor), PorterDuff.Mode.SRC_IN)
                        toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                        TextColorNormalize()
                    } else {
                        duckArr[paragraphIndex].visibility = View.VISIBLE
                        for (i in 0..paragraphIndex) {
                            paragraphArr[i].setTextColor(getColor(R.color._sideColor))
                        }
                        toBmBtn.setColorFilter(getColor(R.color._duckBodyColor), PorterDuff.Mode.SRC_IN)
                        toBmBtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.less_bouncing))
                    }
                }
                delay(1)
            }
        }

        customScrBbuffer = scope.launch {

            var lerp = 0f

            while (true) {

                val sbH = scrollBar.bottom.toFloat()
                val scP = scrollContainer.bottom.toFloat() - scrollBar.scrollY.toFloat()
                val oldScP = scrollContainer.bottom.toFloat()
                val min = 0f
                val max = 1f

                if(oldScP - sbH != 0f) lerp = ((scP - sbH) / (oldScP - sbH)) * (min - max) + max

                val offset = (scrollBar.bottom.toFloat() - getViewHeight(customScrollBarWhite)) * lerp

                customScrollBarRed.translationY = offset * 0.995f
                customScrollBarYellow.translationY = offset * 0.997f
                customScrollBarBlue.translationY = offset * 1.006f
                customScrollBarCyan.translationY = offset * 1.003f
                customScrollBarWhite.translationY = offset

                customScrollBarText.translationY = dpToFloat(50) + offset
                customScrollBarText.translationX = dpToFloat(8)
                customScrollBarText.pivotX = getViewWidth(customScrollBarText) / 2f
                customScrollBarText.pivotY = getViewHeight(customScrollBarText) / 2f
                customScrollBarText.rotation = 90f

                delay(1)
            }
        }
    }
    //
    // очищает сохраненное содержимое внутри "ReadingBlock"
    fun ClearReadingBlock() {

        chText.clear()
        paragraphArr.clear()
        duckArr.clear()
        imageArr.clear()
        imagesArr.clear()
        picContainer.removeAllViews()
        picGalleryScrollContainer.removeAllViews()
        duckLayout.clear()
        storedChDataArr.clear()
        scrollContainer.removeAllViews()

    }
    //
    //восстанавливает цвет текста определенного содержимого до нормального
    private fun TextColorNormalize() {
        //   придает всему тексту обычный (без закладок) цвет
        for (i in 0..paragraphCount) {
            paragraphArr[i].setTextColor(getColor(R.color._textColor))
            scrollPos = 0
        }
        //
    }
    //
    // Блок с выделенными главами
    @SuppressLint("ResourceAsColor")
    fun ChaptersSelectBlock(scope: CoroutineScope) {

        var tomeOld = 0.0
        var tomeOldUI = 0.0

        for (i in 0..chNumArr.lastIndex) {
            chScrollContainerArr += TextView(this)
            chUIscrollContainerArr += TextView(this)
        }
        chJob = scope.launch {
            for (v in chScrollContainerArr) {

                // название главы в режиме выбора главы
                var context = chNumArr[chScrollContainerArr.indexOf(v)].replace(".txt", "")
                var tomeStr = context.substring(0, 3)
                var chapterStr = context.substring(3, context.length)

                var tome = ""
                var chapter = ""

                var tempTome = tomeStr.toDouble().toInt().toDouble()
                var tempChapter = chapterStr.toDouble().toInt().toDouble()

                // система фильтров для придания 000102304230 внешнего вида тома 3, глава 1 (каменное лицо)
                if (tomeStr.toDouble() != tempTome) {
                    tome = tomeStr.toDouble().toString()
                } else {
                    tome = tomeStr.toDouble().toInt().toString()
                }
                if (chapterStr.toDouble() != tempChapter) {
                    chapter = chapterStr.toDouble().toString()
                } else {
                    chapter = chapterStr.toDouble().toInt().toString()
                }

                v.text = "Том $tome, Глава $chapter"

                if (chScrollContainerArr.indexOf(v) == chIndex) {
                    v.setTextColor(getColor(R.color._sideColor))
                } else {
                    v.setTextColor(getColor(R.color._textColor))
                }

                v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

                if (tome.toDouble() == tomeOld) {
                    var separator = layoutInflater.inflate(R.layout.separator_line, null)
                    if (chScrollContainerArr.indexOf(v) != 0)
                        chapterScrollContainer.addView(separator)
                    chapterScrollContainer.addView(v)
                } else {
                    tomeOld = tome.toDouble()

                    var separator = layoutInflater.inflate(R.layout.separator_tri, null)
                    if (chScrollContainerArr.indexOf(v) != 0)
                        chapterScrollContainer.addView(separator)
                    chapterScrollContainer.addView(v)
                }

                v.setOnClickListener {

                    customScrBbuffer.cancel()
                    paragraphBuffer.cancel()
                    picBuffer.cancel()
                    
                    chJob.cancel()
                    chUIjob.cancel()

                    for (i in 0..chScrollContainerArr.lastIndex) {
                        chScrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }
                    for (i in 0..chUIscrollContainerArr.lastIndex) {
                        chUIscrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }

                    v.setTextColor(getColor(R.color._sideColor))
                    chUIscrollContainerArr[chScrollContainerArr.indexOf(v)].setTextColor(
                        getColor(R.color._sideColor)
                    )

                    chScrollPos = v.left
                    chUIscrollPos = chUIscrollContainerArr[chScrollContainerArr.indexOf(v)].top - selectedChH

                    chIndex = chScrollContainerArr.indexOf(v)

                    scrollBar.scrollTo(0, 0)

                    SaveData()

                    chScrollContainerArr.clear()
                    chapterScrollContainer.removeAllViews()
                    chUIscrollContainerArr.clear()
                    chapterUIscrollContainer.removeAllViews()
                    ChaptersSelectBlock(lifecycleScope)
                    ClearReadingBlock()
                    EraseBookMarkData()

                    ReadingBlock(lifecycleScope)
                }
            }
        }
        var text = ""
        chUIjob = scope.launch {

            var height = mutableListOf<Int>()

            for (vUI in chUIscrollContainerArr) {
                // название главы в режиме выбора главы
                var context = chNumArr[chUIscrollContainerArr.indexOf(vUI)].replace(".txt", "")
                var tomeStr = context.substring(0, 3)
                var chapterStr = context.substring(3, context.length)

                var tome = ""
                var chapter = ""

                var tempTome = tomeStr.toDouble().toInt().toDouble()
                var tempChapter = chapterStr.toDouble().toInt().toDouble()

                // система фильтров для придания 000102304230 внешнего вида тома 3, глава 1 (камень л.)
                if (tomeStr.toDouble() != tempTome) {
                    tome = tomeStr.toDouble().toString()
                } else {
                    tome = tomeStr.toDouble().toInt().toString()
                }
                if (chapterStr.toDouble() != tempChapter) {
                    chapter = chapterStr.toDouble().toString()
                } else {
                    chapter = chapterStr.toDouble().toInt().toString()
                }

                vUI.text = "Том $tome, Глава $chapter"

                vUI.gravity = Gravity.CENTER

                if (chUIscrollContainerArr.indexOf(vUI) == chIndex) {

                    vUI.setPadding(0, dpToFloat(24).toInt(), 0, dpToFloat(24).toInt())

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
                    vUI.setTextColor(getColor(R.color._sideColor))
                } else {

                    vUI.setPadding(0, dpToFloat(25).toInt(), 0, dpToFloat(25).toInt())

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                    vUI.setTextColor(getColor(R.color._textColor))
                }


                var separator = layoutInflater.inflate(R.layout.separator_tome, null)
                var tomeText = separator.findViewById<TextView>(R.id._tome_chUI_text)

                tomeText.textAlignment = View.TEXT_ALIGNMENT_CENTER

                tomeText.setPadding(0, dpToFloat(15).toInt(), 0, dpToFloat(20).toInt())

                var sepIndex = 0
                if (tome.toDouble() != tomeOldUI) {
                    tomeOldUI = tome.toDouble()
                    tomeText.text = "Том $tome"
                    chapterUIscrollContainer.addView(separator)

                    sepIndex = chapterUIscrollContainer.indexOfChild(separator)

                    height += getViewHeight(chapterUIscrollContainer)

                }
                chapterUIscrollContainer.addView(vUI)

                selectedChH = getViewHeight(vUI)*3

                vUI.setOnClickListener {

                    customScrBbuffer.cancel()
                    paragraphBuffer.cancel()
                    picBuffer.cancel()
                    
                    chJob.cancel()
                    chUIjob.cancel()

                    for (i in 0..chUIscrollContainerArr.lastIndex) {
                        chUIscrollContainerArr[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        chUIscrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }
                    for (i in 0..chScrollContainerArr.lastIndex) {
                        chScrollContainerArr[i].setTextColor(getColor(R.color._textColor))
                    }

                    vUI.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    vUI.setTextColor(getColor(R.color._sideColor))
                    chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].setTextColor(
                        getColor(R.color._sideColor)
                    )

                    chScrollPos = chScrollContainerArr[chUIscrollContainerArr.indexOf(vUI)].left
                    chUIscrollPos = vUI.top - selectedChH

                    chIndex = chUIscrollContainerArr.indexOf(vUI)

                    scrollBar.scrollTo(0, 0)
                    chapterScrollBar.scrollTo(chScrollPos, 0)

                    chScrollContainerArr.clear()
                    chapterScrollContainer.removeAllViews()
                    chUIscrollContainerArr.clear()
                    chapterUIscrollContainer.removeAllViews()
                    ChaptersSelectBlock(lifecycleScope)
                    SaveData()
                    ClearReadingBlock()
                    EraseBookMarkData()
                    ClearChSelBlock()

                    ReadingBlock(lifecycleScope)
                }
            }
            var switch = 0
            while (true) {

                val curScr = chapterUIscrollBar.scrollY
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                fun tomeAnim() {
                    chapterUIsvg.alpha = 0f
                    chapterUIsvg.scaleX = 0.4f
                    chapterUIsvg.scaleY = 0.4f
                    chapterUIsvg.animate().alpha(1f).setDuration(150).start()
                    chapterUIsvg.animate().scaleX(0.6f).setDuration(150).start()
                    chapterUIsvg.animate().scaleY(0.6f).setDuration(150).start()
                }

                if (curScr < oldScr && of == 0) {
                    oldScr = height[of]
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        oldOf = of
                    }
                }
                else if (curScr < oldScr && of != 0) {
                    oldScr = height[of]
                    of-=1
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of
                    }
                }
                else if(curScr >= height[of] && of != height.lastIndex) {
                    oldScr = height[of]
                    of+=1
                    if(oldOf != of) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of
                    }
                }
                else if (curScr >= height[of] && of == height.lastIndex) {
                    oldScr = height[of]
                    if(oldOf != of+1) {
                        chapterUIsvg.setImageResource(chUImidSvg.random())
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                        tomeAnim()
                        oldOf = of+1
                    }
                }
                delay(1)
            }
        }
    }
    fun ClearChSelBlock() {
        isChUIshown = false

        openChUIbtn.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_from_180_fa))
        delRanobeBtn.visibility = View.GONE
        duckCustomizerBtn.visibility = View.VISIBLE
        chapterUIsvg.visibility = View.GONE

        chapterScrollBar.visibility = View.VISIBLE

        scrollContainer.animate().alpha(1f).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
        picContainer.animate().alpha(1f).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()

        chapterShownUI.animate().translationY(dpToFloat(500)).setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator()).start()
    }
    //
    fun getViewHeight(view: View): Int {
        val wm = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val deviceWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val size = Point()
            display.getSize(size)
            size.x
        } else {
            display.width
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        return view.measuredHeight //        view.getMeasuredWidth();
    }
    fun getViewWidth(view: View): Int {
        val wm = view.context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val deviceWidth: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val size = Point()
            display.getSize(size)
            size.x
        } else {
            display.width
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        return view.measuredWidth //        view.getMeasuredWidth();
    }
    // Блок с меню приветствия / загрузки
    fun WelcomeViewBlock() {

        EraseChData()
        EraseBookMarkData()
        SaveData()

        // clears UI
        header.visibility = View.GONE
        chapterScrollBlock.visibility = View.GONE
        chapterUIscrollBlock.visibility = View.GONE
        //

        centeredBlock.visibility = View.VISIBLE

        val welcomeView = layoutInflater.inflate(R.layout.welcome_view, null)

        // поиск идентификатора элемента внутри welcomeView
        val nothingHere = welcomeView.findViewById<TextView>(R.id._nothingHere)
        val helpInfo = welcomeView.findViewById<TextView>(R.id._helpInfo)
        val addRanobeBtn = welcomeView.findViewById<ImageView>(R.id._addRanobeBtn)
        val gitLinkBtn = welcomeView.findViewById<ImageView>(R.id._gitLinkBtn)
        val ranobeInput = welcomeView.findViewById<LinearLayout>(R.id._ranobeInput)
        val hideRanobeBtn = welcomeView.findViewById<ImageView>(R.id._hideRanobeBtn)
        val bigDuck = welcomeView.findViewById<ImageView>(R.id._bigDuck)
        val urlSearch = welcomeView.findViewById<EditText>(R.id._urlSearch)
        //
        // генерация welcomeView
        centeredBlock.addView(welcomeView)
        //
        ranobeInput.visibility = View.GONE
        hideRanobeBtn.visibility = View.GONE

        // анимация мигающего курсора
        val animator = ValueAnimator.ofFloat(0f, 2f).apply {
            addUpdateListener { animation ->
                if (animation.animatedValue as Float <= 1f) nothingHere.text =
                    "Добавьте старую ссылку на сайт ranobelib.com!!!"
                else nothingHere.text = "Добавьте старую ссылку на сайт ranobelib.com!!!"
            }
            duration = 1000
            repeatCount = Animation.INFINITE
            start()
        }
        //

        // действие кнопки "git link" при нажатии
        gitLinkBtn.setOnClickListener {
            gitLinkBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.less_bouncing))
            val url = "https://github.com/"

            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        //

        // "разобрать ссылку с помощью кнопки ranobelib.com", активируемой при нажатии
        addRanobeBtn.setOnClickListener {

            if (gitLinkBtn.visibility == View.VISIBLE) {

                gitLinkBtn.visibility = View.GONE
                ranobeInput.visibility = View.VISIBLE

                addRanobeBtn.animate().scaleX(0.8f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                addRanobeBtn.animate().scaleY(0.8f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                addRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_45_to))

                ranobeInput.alpha = 0f
                ranobeInput.animate().alpha(1f).setDuration(250).start()

                ranobeInput.scaleX = 0.8f
                ranobeInput.scaleY = 0.8f

                ranobeInput.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                ranobeInput.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            } else {
                gitLinkBtn.visibility = View.VISIBLE
                ranobeInput.visibility = View.GONE

                addRanobeBtn.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                addRanobeBtn.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()

                addRanobeBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_45_from))

                gitLinkBtn.alpha = 0f
                gitLinkBtn.animate().alpha(1f).setDuration(250).start()

                gitLinkBtn.scaleX = 1.2f
                gitLinkBtn.scaleY = 1.2f

                gitLinkBtn.animate().scaleX(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
                gitLinkBtn.animate().scaleY(1f).setDuration(250)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
        //

        // проанализировать ввод URL-адреса во временную папку
        urlSearch.setOnKeyListener(object : View.OnKeyListener {
            @SuppressLint("RestrictedApi")
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        if(!isDownloading) {
                            Thread {
                                DownloadRanobe("${urlSearch.text}")
                                runOnUiThread {
                                    //Update UI
                                    if(wrongInputURL) {

                                        RedImpulse(urlSearch, ranobeInput)
                                        when (c) {
                                            1 -> {
                                                coolToast(R.drawable.duck_customizer,"Приложение поддерживает только ссылки с ranobelib.me.")
                                                c = 2
                                            }
                                            2 -> {
                                                coolToast(R.drawable.duck_customizer,"В будущем можно будет использовать и другие платформы")
                                                c = 1
                                            }
                                        }
                                        wrongInputURL = false
                                    }
                                    if(successConnect) {

                                        GreenImpulse(urlSearch, ranobeInput)

                                        successConnect = false
                                    }
                                    if(parsingError) {

                                        DeleteChapters()

                                        val cancelAfterDelay = lifecycleScope.launch {

                                            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                            notificationManager.cancel(NOTIFICATION_ID)

                                            ShowNotification(
                                                "Ошибка",
                                                "Что-то пошло не так, попробуйте загрузить главы по новой, но это не точно."
                                            )

                                            RedImpulse(urlSearch, ranobeInput)

                                            parsingError = false

                                            isDownloading = false

                                            delay(1000)
                                        }


                                    }
                                    if(doesLastChapterDownloaded) {

                                        //изменить его при получении уведомления

                                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                        notificationManager.cancel(NOTIFICATION_ID)

                                        ShowNotification(
                                            "Загрузка завершена",
                                            "Загружено ${chaptersAmount} глав(ы). Если число не сходится с сайтом, попробуйте снова"
                                        )

                                        // скрыть клавиатуру при загрузке основного блока
                                        fun View.hideKeyboard() {
                                            val inputManager = context.getSystemService(
                                                INPUT_METHOD_SERVICE
                                            ) as InputMethodManager
                                            inputManager.hideSoftInputFromWindow(windowToken, 0)
                                        }
                                        scrollContainer.hideKeyboard()
                                        //
                                        doesLastChapterDownloaded = false

                                        isDownloading = false

                                        val restartAfterDelay = lifecycleScope.launch {
                                            delay(10)
                                            RestartApp(applicationContext)
                                        }
                                    }
                                }
                            }.start()
                        }
                        else {
                            coolToast(R.drawable.duck_customizer,"Скачивание уже началось, ожидайте, чем больше будешь тыкать, тем меньше толку")
                        }
                    }
                    else -> return false
                }
                return true
            }
        })
        //
    }
    //
    // генерирует "контейнер условий", чтобы убедиться, что приложение может работать должным образом при выполнении приведенного ниже кода
    fun AllowPermissionBlock() {

        centeredBlock.visibility = View.VISIBLE

        RequestPermission()

        val permView = layoutInflater.inflate(R.layout.perm_view, null)
        // очищает пользовательский интерфейс
        header.visibility = View.GONE
        chapterScrollBlock.visibility = View.GONE
        chapterUIscrollBlock.visibility = View.GONE
        //

        // генерирует "представление запроса"
        centeredBlock.addView(permView)
        //

        // анимация мигающего курсора
        val nothingHere = permView.findViewById<TextView>(R.id._permText)
        val animator = ValueAnimator.ofFloat(0f, 2f).apply {
            addUpdateListener { animation ->
                if (animation.animatedValue as Float <= 1f) {
                    nothingHere.text =
                        "Откройте приложению доступ к файлам и перезагрузите его, чтобы продолжить использование_"
                } else nothingHere.text =
                    "Откройте приложению доступ к файлам и перезагрузите его, чтобы продолжить использование"
            }
            duration = 1000
            repeatCount = Animation.INFINITE
            start()
        }
        //
    }
    //
    // создает определенные папки и файлы данных
    fun CreateContentStorage() {
        // software folder
        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }
        //

        // папка с главами
        val chFolder = File(appFolder, chFolderName)
        if (!chFolder.exists()) {
            chFolder.mkdirs()
        }
        //

        // папка с изображениями
        val picFolder = File(appFolder, picFolderName)
        if (!picFolder.exists()) {
            picFolder.mkdirs()
        }
        //

        // создает данные закладок
        if (!File(appFolder, bMdataFileName).exists()) {
            val readerData = File(appFolder, bMdataFileName)
            val writerData = FileWriter(readerData)
            writerData.append("No data")
            writerData.flush()
            writerData.close()
        }
        //

        // создает приостановленные данные главы, если они не существуют
        if (!File(appFolder, chDataFileName).exists()) {
            val readerChData = File(appFolder, chDataFileName)
            val writerChData = FileWriter(readerChData)
            writerChData.append("No data")
            writerChData.flush()
            writerChData.close()
        }
        //

        // создает данные заголовка, если они не существуют
        if (!File(appFolder, ranobeTitleFileName).exists()) {
            val readerChData = File(appFolder, ranobeTitleFileName)
            val writerChData = FileWriter(readerChData)
            writerChData.append("No data")
            writerChData.flush()
            writerChData.close()
        }
        //
    }
    //
    // сохраняет (записывает) данные в файлы
    fun SaveData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // создает файл для хранения текущего состояния закладки
        val readerData = File(appFolder, bMdataFileName)
        val writerData = FileWriter(readerData)
        writerData.append("${bookMarked}\n${paragraphIndex}\n$scrollPos")
        writerData.flush()
        writerData.close()
        //

        // создает файл для хранения номера текущей главы
        val readerChData = File(appFolder, chDataFileName)
        val writerChData = FileWriter(readerChData)
        writerChData.append("$chIndex\n$chScrollPos\n$chUIscrollPos")
        writerChData.flush()
        writerChData.close()
        //

        val readerTiData = File(appFolder, ranobeTitleFileName)
        val writerTiData = FileWriter(readerTiData)
        writerTiData.append("$ranobeTitle")
        writerTiData.flush()
        writerTiData.close()
    }
    //
    // загружает данные закладок из storedBmDataArr[]
    fun LoadBookMarkData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // сохраняет данные файла в storedBmDataArr[]
        val dataFile = File("$appFolder/${bMdataFileName}")
        for (line in dataFile.readLines()) {
            storedBmDataArr += line
        }
        //
        // если "chStoredData.txt" является странным
        try {
            bookMarked = storedBmDataArr[0].toBoolean()
            paragraphIndex = storedBmDataArr[1].toInt()
            scrollPos = storedBmDataArr[2].toInt()
        } catch (e: Exception) {

            bookMarked = false
            paragraphIndex = 0
            scrollPos = 0

            SaveData()

            coolToast(R.drawable.duck_customizer,"Ошибка: B0M1DU6K")
        }
    }
    //
    // загружает данные главы в chText[] (в будущем "i" этого массива будет "абзацами"
    @SuppressLint("RestrictedApi")
    fun LoadChapterData() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        // загружает данные заголовка в переменную
        val titleFile = File("$appFolder/${ranobeTitleFileName}")
        for (line in titleFile.readLines()) {
            ranobeTitle = line
        }
        //

        // сохраняет данные файла в storedChArr[]
        val dataChFile = File("$appFolder/${chDataFileName}")
        for (line in dataChFile.readLines()) {
            storedChDataArr += line
        }
        //

        // если "chStoredData.txt" является странным
        try {
            chIndex = storedChDataArr[0].toInt()
            chScrollPos = storedChDataArr[1].toInt()
            chUIscrollPos = storedChDataArr[2].toInt()
        } catch (e: Exception) {

            chIndex = 0
            chScrollPos = 0
            chUIscrollPos = 0

            SaveData()

            coolToast(R.drawable.duck_customizer,"Ошибка: CH3RDU6K")
        }

        // номер массива равен выбранной главе
        var chName = chNumArr[chIndex]
        //

        // если какое-либо загруженное "название главы" из "массива глав" больше недоступно, возвращает fun с ошибкой
        if (!File("$appFolder/${chFolderName}/$chName").exists()) {
            coolToast(R.drawable.duck_customizer, "Ошибка: N0DU6K")
            return
        }
        //

        // если найдена определенная глава, добавляет ее "содержимое абзацев" в chText[]
        val chContent = File("$appFolder/${chFolderName}/$chName")
        for (line in chContent.readLines().filter {
            !it.contains("<div class=\"article-image\">") &&
                    !it.contains("<img class=\"lazyload\"") &&
                    !it.contains("</div>")
        }) {
            chText += line
        }
        //
    }
    //
    // удаляет все данные из storedData.txt
    fun EraseBookMarkData() {

        bookMarked = false
        paragraphIndex = 0
        scrollPos = 0
        storedBmDataArr.clear()

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val readerData = File(appFolder, bMdataFileName)
        val writerData = FileWriter(readerData)
        writerData.append("${bookMarked}\n${paragraphIndex}\n$scrollPos")
        writerData.flush()
        writerData.close()

    }
    //
    // удаляет все данные из chStoredData.txt
    fun EraseChData() {

        chIndex = 0
        chScrollPos = 0

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val readerChData = File(appFolder, chDataFileName)
        val writerChData = FileWriter(readerChData)
        writerChData.append("$chIndex\n$chScrollPos")
        writerChData.flush()
        writerChData.close()
    }
    //
    // поиск любой главы в папке "Главы"
    fun ChNumsSearchAndSort() {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)

        val chFolder = File("$appFolder/${chFolderName}")

        for (file in chFolder.list()) {
            chNumArr += file
        }
        chNumArr.sort()
    }
    //
    // удаляет все содержимое из папок приложений
    fun DeleteChapters() {
        var appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val chFolder = File(appFolder, chFolderName)
        val picFolder = File(appFolder, picFolderName)
        for (chapters in chFolder.listFiles()) {
            chapters.delete()
        }
        for (f in picFolder.listFiles()) {
            for( p in f.listFiles()) {
                p.delete()
            }
            f.delete()
        }
    }
    //
    // канал уведомлений
    fun CreateNotificationChannel() {
        val name = "Quack!"
        val descriptionText = "Утка села на шпагат, Куплинов знает толк"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = descriptionText
        // Создайте новый исходный файл на основе выбранного файла
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    //
    // уведомление, которое может быть закрыто
    @SuppressLint("MissingPermission")
    fun ShowNotification(title: String, text: String) {
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setColor(Color.TRANSPARENT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())

        with(NotificationManagerCompat.from(this)) {
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }
    //
    // уведомление, которое не может быть закрыто
    @SuppressLint("MissingPermission")
    fun ShowImmortalNotification(title: String, text: String) {
        val builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setOngoing(true)
            .setColor(Color.TRANSPARENT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())

        with(NotificationManagerCompat.from(this)) {
            notify(MainActivity.NOTIFICATION_ID, builder.build())
        }
    }
    //
    // проверяет разрешения для различных API-интерфейсов
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun CheckPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android-версия 11 (R) или выше
            Environment.isExternalStorageManager()
        } else {
            //Android ниже 11 (R)
            val note = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            val internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            val write = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return (
                note == PackageManager.PERMISSION_GRANTED &&
                internet == PackageManager.PERMISSION_GRANTED &&
                write == PackageManager.PERMISSION_GRANTED &&
                read == PackageManager.PERMISSION_GRANTED
            )
        }
    }
    //
    // проверяет доступность интернет-соединения
    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }
    //
    // загружает весь контент целиком по нужным ссылкам
    @SuppressLint("RestrictedApi")
    fun DownloadRanobe(url: String) {

        var finurl = url
        // проверка состояния доступности URL-адреса
        try {
            finurl = if(!finurl.contains("https://")) {
                "https://$url"
            } else url
            isDownloading = true
            inputURL = URL(finurl)

            if(!finurl.contains("ranobelib.me")) {
                wrongInputURL = true
                isDownloading = false
                return
            } else successConnect = true
        } catch (e: Exception) {
            wrongInputURL = true
            isDownloading = false
            return
        }
        //

        // этот блок является начальной точкой синтаксического анализа. здесь программа пытается найти главы
        nextChapterFromHTML = "$finurl"
            .substringBeforeLast("?ui")
            .substringBeforeLast("&ui")
            .substringBeforeLast("?page")

        var isSecondParse = false

        while (isDownloading) {

            if(!isNetworkAvailable(applicationContext)) {
                ShowNotification(
                    "Нет подключения к интернету",
                    "Проверьте подключение к интернету, чтобы продолжить"
                )
            }
            else {
                var noteText = "v${nextChapterFromHTML.substringAfterLast("v").substringBefore("?bid")}"
                    .replace("v", "Том ")
                    .replace("/c", ", Глава ")

                if(!isSecondParse)
                    ShowImmortalNotification(
                        "Идёт загрузка глав...",
                        "$noteText"
                    )
                if(isSecondParse)
                    ShowImmortalNotification("Проверка глав...", "$noteText")

                try {
                    val appFolder = File(Environment.getExternalStorageDirectory(),
                        appFolderName
                    )
                    val anotherFolder = File(appFolder, chFolderName)

                    Log.i("TEXT", "${nextChapterFromHTML}")

                    val doc = Jsoup.connect(nextChapterFromHTML).userAgent("Mozilla").get()
                    val html = doc.outerHtml()
                    var getText = ""

                    if (html.contains("reader-header-action__text text-truncate")) {
                        ranobeTitle = html
                            .substringAfterLast("<div class=\"reader-header-action__text text-truncate\">")
                            .substringBefore("</div>")
                            .replace("       ", "")
                            .replace("\n","")
                            .replace("      ","")
                        SaveData()
                    }

                    getText = html
                        .substringAfter("<div class=\"reader-container container container_center\">")
                        .substringBefore("</div> <!-- --> <!-- -->")
                        .replace("<p>","")
                        .replace("</p>","")
                        .replace("&nbsp;","")
                        .replace("    ","")
                        .substringAfter("\n")
                        .substringBeforeLast("\n")

                    var volume = nextChapterFromHTML
                        .substringAfterLast("/v")
                        .substringBeforeLast("/c")
                        .substringBefore(".0")

                    if (volume.toDouble() < 10) {
                        volume = "00$volume"
                    }
                    else if(volume.toDouble() < 100) {
                        volume = "0$volume"
                    }

                    var chapter = nextChapterFromHTML
                        .substringAfterLast("/c")
                        .substringBefore(".0")
                        .substringBefore("?bid")

                    if (chapter.toDouble() < 10) {
                        chapter = "00$chapter"
                    }
                    else if(chapter.toDouble() < 100) {
                        chapter = "0$chapter"
                    }

                    for (line in html.lines()) {

                        if(line.contains("<img class=\"lazyload\"")) {
                            val picture = line
                                .substringAfter("<div class=\"reader-container container container_center\">")
                                .substringBefore("</div> <!-- --> <!-- -->")
                                .substringAfter("<img class=\"lazyload\" data-background=\"\" data-src=\"")
                                .substringBefore("\">")
//                            Log.i("PICTURE", "$picture")
                            val picurl = URL(picture)
                            val image = BitmapFactory.decodeStream(picurl.openConnection().getInputStream())
                            SaveImage("$volume$chapter", "${picture.substringAfterLast("/").substringBeforeLast(".jpg").substringBeforeLast(".png")}", image)
                        }
                        else {
                            Log.i("0", "0")
                        }

                        val readerChData = File(anotherFolder, "$volume$chapter.txt")
                        val writerChData = FileWriter(readerChData)
                        writerChData.append("$getText")
                        writerChData.flush()
                        writerChData.close()

                        // идёт двойная загрузка из-за касяков
                        if(line.contains("Последняя глава прочитана") && isSecondParse) {
                            chaptersAmount = anotherFolder.listFiles().size
                            doesLastChapterDownloaded = true
                            isDownloading = false
                            return
                        }
                        else if (line.contains("Последняя глава прочитана") && !isSecondParse) {
                            nextChapterFromHTML = "$finurl"
                                .substringBeforeLast("?ui")
                                .substringBeforeLast("&ui")
                                .substringBeforeLast("?page")
                            isSecondParse = true
                        }
                        //
                        if(line.contains("class=\"reader-next__btn button text-truncate button_label button_label_right\"")) {
                            nextChapterFromHTML = line
                                .substringAfterLast("<a class=\"reader-next__btn button text-truncate button_label button_label_right\" href=\"")
                                .substringBefore("\" tabindex=\"-1\">")
                                .substringBeforeLast("?ui")
                                .substringBeforeLast("&ui")
                                .substringBeforeLast("?page")
                        }
                        else {
                            Log.i("0", "0")
                        }
                        //
                    }
                } catch (e: Exception) {

                }
                //
            }
        }
    }
    //
    // сохраняет изображения из URL-адресов
    @Throws(IOException::class)
    fun SaveImage(folderName : String, imgName: String, bm: Bitmap) {

        val appFolder = File(Environment.getExternalStorageDirectory(), appFolderName)
        val picFolder = File(appFolder, picFolderName)
        // папка с изображениями
        val chPicFolder = File(picFolder, folderName)
        if (!chPicFolder.exists()) {
            chPicFolder.mkdirs()
        }
        //

        val imageFile = File(chPicFolder, "$imgName.png")
        val out = FileOutputStream(imageFile)

        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out) // Compress Image
            out.flush()
            out.close()

            // Сообщите сканеру мультимедиа о новом файле, чтобы он был доступен
            // немедленно доступны пользователю.
            MediaScannerConnection.scanFile(applicationContext, arrayOf<String>(imageFile.absolutePath), null) { path, uri ->

            }
        } catch (e: java.lang.Exception) {
            throw IOException()
        }
    }
    //
    // перезапускает приложение, когда это необходимо
    fun RestartApp(context : Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
    //
    // запрашивает разрешения для различных API-интерфейсов
    @SuppressLint("RestrictedApi")
    fun RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) or above
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)

                if (Build.VERSION.SDK_INT > 32) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATIONS_PERMISSION_CODE
                    )
                }

            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Android is below 11(R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.INTERNET
                ),
                INTERNET_PERMISSION_CODE
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                NOTIFICATIONS_PERMISSION_CODE
            )
        }
    }
    //
    @SuppressLint("RestrictedApi")
    val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //здесь мы рассмотрим результат нашего "намерения"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11(R) or above
                if (Environment.isExternalStorageManager()) {
                    //Получено разрешение на управление внешним хранилищем
                } else {
                    //В разрешении на управление внешним хранилищем отказано....
                }
            } else {
                //Android is below 11(R)
            }
        }
    //
    // то же, что и в случае с описанным выше
    @SuppressLint("RestrictedApi")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                //то же, что и в случае с описанным выше
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    //Получено разрешение на внешнее хранение
                } else {
                    //В разрешении на внешнее хранилище отказано
                }
            }
        }
        if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                //проверить каждое разрешение, предоставлено оно или нет
                val internet = grantResults[2] == PackageManager.PERMISSION_GRANTED
                if (internet) {
                    //Получено разрешение на внешнее хранение
                } else {
                    //В разрешении на внешнее хранилище отказано...
                }
            }
        }
        if (requestCode == NOTIFICATIONS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                //проверить каждое разрешение, предоставлено оно или нет
                val note = grantResults[3] == PackageManager.PERMISSION_GRANTED
                if (note) {
                    //Получено разрешение на внешнее хранение
                } else {
                    //В разрешении на внешнее хранилище отказано...
                }
            }
        }
    }
    //
    // преобразует dp в float
    fun dpToFloat(sizeInDp: Int): Float {
        var screenOffset = resources.displayMetrics.density
        return (sizeInDp * screenOffset)
    }
    //

    // генерирует всплывающее сообщение при вызове
    fun coolToast(icon: Int, text: String, lifeTime: Long = 3000) {

        var toastView = layoutInflater.inflate(R.layout.duck_toast, null)
        var toastTextId = toastView.findViewById<TextView>(R.id._toast_text)
        var toastPicId = toastView.findViewById<ImageView>(R.id._toast_pic)

        toastView.animation = AnimationUtils.loadAnimation(applicationContext, R.anim.toast_appearing)

        try {
            toastContainer.addView(toastView)
        } catch (e: Exception) {
            toastContainer.removeView(toastView)
            toastContainer.addView(toastView)
        }

        toastPicId.setImageResource(icon)
        try {
            val animatable = toastPicId.drawable as Animatable
            animatable.start()
        } catch (e: Exception) {

        }
        toastTextId.text = text

        val lifeTimeToast = lifecycleScope.launch {
            delay(lifeTime)
            toastView.animate().alpha(0f).setDuration(220).start()
        }

        val removeToast = lifecycleScope.launch {
            while (true) {
                if(toastView.alpha == 0f) {
                    toastContainer.removeView(toastView)
                }
                delay(1)
            }
        }
    }
    //

    // При ошибке в строке url (см. блок приветствия)
    fun RedImpulse(view : EditText, animatedView : LinearLayout) {

        val backgroundDrawable = DrawableCompat.wrap(view.background).mutate()

        DrawableCompat.setTint(backgroundDrawable, Color.parseColor("#FF3C3C"))
        val boxPos = ValueAnimator.ofFloat(0f, 30f, -30f, 0f).apply {
            addUpdateListener { animation ->
                animatedView.translationX = animation.animatedValue as Float
            }
            duration = 220
            start()
        }
        view.setTextColor(Color.parseColor("#ffffff"))
        view.setHintTextColor(Color.parseColor("#ffffff"))
    }
    //

    // При успешном отображении в строке url (см. блок приветствия)
    fun GreenImpulse(view : EditText, animatedView : LinearLayout) {

        val backgroundDrawable = DrawableCompat.wrap(view.background).mutate()

        DrawableCompat.setTint(backgroundDrawable, Color.parseColor("#3CFF42"))
        view.setTextColor(getColor(R.color._darkerMainColor))
        view.setHintTextColor(getColor(R.color._darkerMainColor))
    }
    //
    // проверяет текущее положение прокрутки /-> требуется только при запуске приложения, когда абзацы все еще загружаются в буфер
    fun CheckScrollPos(scope: CoroutineScope) {
        var job = scope.launch {
            delay(600)
            if (scrollBar.scrollY != scrollPos) {
                coolToast(R.drawable.tobookmark,"Глава ещё прогружается, пожалуйста подождите...")
            }
        }
    }
    //
}