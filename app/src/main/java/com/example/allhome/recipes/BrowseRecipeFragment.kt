package com.example.allhome.recipes

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.webkit.*
import android.webkit.WebView.HitTestResult
import android.webkit.WebView.RENDERER_PRIORITY_BOUND
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.FragmentBrowseRecipeBinding
import com.example.allhome.global_ui.ProgressDialogFragment
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.utils.ImageUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.*
import okio.*
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BrowseRecipeFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    lateinit var mFragmentBrowseRecipeBinding:FragmentBrowseRecipeBinding
    lateinit var mSearchView:SearchView

    var mRecipeEntity:RecipeEntity? = null
    var mIngredients = arrayListOf<IngredientEntity>()
    var mRecipeStepEntities = arrayListOf<RecipeStepEntity>()
    var mProgressDialogFragment:ProgressDialogFragment = ProgressDialogFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        mFragmentBrowseRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_browse_recipe, container, false)
        mFragmentBrowseRecipeBinding.progressBar.max = 100
        mFragmentBrowseRecipeBinding.parseButton.setOnClickListener {
            mFragmentBrowseRecipeBinding.webview.loadUrl("javascript:(function(){let doc = \"<html>\"+(document.documentElement.innerHTML)+\"<html>\";window.JavascriptBridge.getHtmlAsString(doc)})()")
        }
        val toolBar = mFragmentBrowseRecipeBinding.toolbar
        toolBar.title = "Browse recipe"
        toolBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolBar.setNavigationOnClickListener {
            activity?.finish()
        }

        toolBar.inflateMenu(R.menu.search_recipe_online_menu)
        val menu = toolBar.getMenu()
        mSearchView = menu.findItem(R.id.appBarSearch).actionView as SearchView
        mSearchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                mFragmentBrowseRecipeBinding.webview.loadUrl("https://www.google.com/search?q=${query}")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        setupViewView()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                if(mFragmentBrowseRecipeBinding.webview.canGoBack()){
                    mFragmentBrowseRecipeBinding.webview.goBack()
                    return
                }
                activity?.finish()

            }

        })

        registerForContextMenu(mFragmentBrowseRecipeBinding.webview)

        return mFragmentBrowseRecipeBinding.root
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val webViewHitTestResult: HitTestResult = mFragmentBrowseRecipeBinding.webview.getHitTestResult()

        if (webViewHitTestResult.type == HitTestResult.IMAGE_TYPE || webViewHitTestResult.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            menu.setHeaderTitle("Download Image...")

            menu.add(0, 1, 0, "Click to download")
                .setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener{
                    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                        val DownloadImageURL = webViewHitTestResult.extra
                        if (URLUtil.isValidUrl(DownloadImageURL)) {
//                            val mRequest = DownloadManager.Request(Uri.parse(DownloadImageURL))
//                            mRequest.allowScanningByMediaScanner()
//                            mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                            Toast.makeText(requireContext(), "Image Downloaded Successfully...", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Sorry.. Something Went Wrong...", Toast.LENGTH_LONG).show()
                        }
                        return false
                    }
                })
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.search_recipe_online_menu, menu)
//        val menutItem = menu.findItem(R.id.appBarSearch)
//        mSearchView = menutItem.actionView as SearchView
//        mSearchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//                mFragmentBrowseRecipeBinding.webview.loadUrl("https://www.google.com/search?q=${query}")
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return true
//            }
//
//        })
//
//    }

     val webChromeClint = object: WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            Log.e("PROGRESS","${newProgress}")
            mFragmentBrowseRecipeBinding.progressBar.progress = newProgress

            super.onProgressChanged(view, newProgress)
        }


    }

    fun setupViewView(){
        mFragmentBrowseRecipeBinding.webview.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                mSearchView.setQuery(url,false)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.e("loaded","${url}")
                super.onPageFinished(view, url)
            }


        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFragmentBrowseRecipeBinding.webview.setRendererPriorityPolicy(RENDERER_PRIORITY_BOUND, true)
        }
        mFragmentBrowseRecipeBinding.webview.webChromeClient =webChromeClint
        mFragmentBrowseRecipeBinding.webview.settings.domStorageEnabled = true
        mFragmentBrowseRecipeBinding.webview.settings.javaScriptEnabled = true// disable the default zoom controls on the page
        mFragmentBrowseRecipeBinding.webview.apply {
            settings.javaScriptEnabled = true
            //loadUrl("https://www.allrecipes.com/recipe/220167/cuban-style-picadillo/")
            //loadUrl("https://panlasangpinoy.com/filipino-pork-menudo-recipe")
            //loadUrl("https://www.foodnetwork.com/recipes/food-network-kitchen/the-best-baked-salmon-1-8081733")
            //loadUrl("https://www.epicurious.com/recipes/food/views/spiced-salmon-kebabs-51169490")
            //loadUrl("https://tasty.co/recipe/apple-pie-from-scratch")
            //loadUrl("https://www.yummly.com/recipe/DIY-Popeyes-Chicken-Sandwich-9116642")
            //loadUrl("https://spoonacular.com/recipes/instant-pot-crack-chicken-chili-1516713")//microdata markup NOT OK, NO STEPS
            //loadUrl("https://www.delish.com/cooking/recipe-ideas/a30195933/easy-hot-crab-dip-recipe/")
            //loadUrl("https://www.bbcgoodfood.com/recipes/sausage-mushroom-ragu")
            //loadUrl("https://www.bettycrocker.com/recipes/gremolata-topped-sea-bass/fb6de1f5-4068-43cc-90cf-1edacafb8763")
            //loadUrl("https://www.bigoven.com/recipe/slow-roasted-tomato-basil-soup/188493")
            //loadUrl("https://www.bonappetit.com/recipe/peperoni")
            //loadUrl("https://www.chowhound.com/recipes/mexican-chorizo-beef-ragu-with-cheese-toast-32198")
            //loadUrl("https://www.cooksillustrated.com/recipes/14127-pastelon-puerto-rican-sweet-plantain-and-picadillo-casserole")
            //loadUrl("https://www.cooks.com/recipe/hf8zp2nr/dill-potato-salad.html")// not working
            //loadUrl("https://www.eatingwell.com/recipe/276923/slow-cooker-mushroom-soup-with-sherry/")
            //loadUrl("https://www.epicurious.com/recipes/food/views/mushroom-adobo")
            //loadUrl("https://www.finecooking.com/recipe/warm-black-bean-chipotle-dip")
            //loadUrl("https://www.foodandwine.com/recipes/arroz-de-galinha-portuguese-chicken-and-rice")
            //loadUrl("https://www.foodnetwork.com/recipes/food-network-kitchen/the-best-baked-salmon-1-8081733")
            //loadUrl("https://www.food.com/recipe/mediterranean-turkey-meatball-sandwiches-pita-or-wrap-117806")
            //loadUrl("https://www.goodfood.com.au/recipes/caramelised-onion-sambal-20210924-h1yskh")
            //loadUrl("https://www.hungry-girl.com/weekly-recipes/sriracha-teriyaki-wings")
            //loadUrl("https://www.joyofbaking.com/Biscuits.html") not working
            //loadUrl("https://www.kingarthurbaking.com/recipes/king-arthurs-original-cake-pan-cake-recipe")
            //loadUrl("https://www.mygourmetconnection.com/grilled-turkey-salad-low-fat-avocado-dressing/")
            //loadUrl("https://www.myrecipes.com/recipe/beer-braised-brisket-1")
            //loadUrl("https://cooking.nytimes.com/recipes/1022565-pepperoni-pasta-with-lemon-and-garlic?action=click&region=Sam%20Sifton%27s%20Suggestions&rank=1")
            //loadUrl("https://www.pbs.org/food/recipes/chicken-makbous/")// unformatted not working
            //loadUrl("https://www.pillsbury.com/recipes/easy-pork-and-squash-sheet-pan-dinner/b4ec18bd-2166-4a86-bd4d-856b0b3d49b1")
            //loadUrl("https://www.saveur.com/article/Recipes/Classic-Steak-Diane/") //RDFa format
            //loadUrl("https://www.seriouseats.com/eetch-armenian-bulgur-tomato-herb-salad-5202151") // not ok delay loading of application/ld+json
            //loadUrl("https://www.simplyrecipes.com/recipes/new_jersey_italian_hot_dog/")
            //loadUrl("https://www.tasteofhome.com/recipes/bbq-chicken-and-apple-bread-pudding/")
            //loadUrl("https://tastykitchen.com/recipes/special-dietary-needs/crusted-tofu/") //Microdata
            //loadUrl("https://www.the-girl-who-ate-everything.com/keto-egg-roll-in-a-bowl/")
            //loadUrl("https://www.thepioneerwoman.com/food-cooking/recipes/a10387/pumpkin-smoothie/")
            //loadUrl("https://www.washingtonpost.com/recipes/chickpea-pancake-mushrooms-and-apple/18011/")
            //loadUrl("https://www.womansday.com/food-recipes/food-drinks/recipes/a58503/quick-beef-ragu-recipe/")
            //loadUrl("https://www.bhg.com/recipe/cider-braised-chicken-brussels-sprouts-and-apples/")
            //loadUrl("https://www.canadianliving.com/food/lunch-and-dinner/recipe/spaghetti-with-brussels-sprouts") // not ok delay loading of application/ld+json
            //loadUrl("https://www.goodhousekeeping.com/food-recipes/a37199514/meatball-sub-sandwich-recipe/")
            //loadUrl("https://www.midwestliving.com/recipe/delicata-frittata/")
            //loadUrl("https://www.realsimple.com/food-recipes/browse-all-recipes/baked-sweet-potatoes-with-feta-butter-recipe")
            //loadUrl("https://www.marthastewart.com/1545825/light-and-bright-beef-stew")
            //loadUrl("https://www.yummy.ph/recipe/chicken-salpicao-recipe-a1793-20210806?ref=featured")
            //loadUrl("https://panlasangpinoy.com/grilled-liempo-with-barbecue-sauce/#recipe")
            //loadUrl("https://www.panlasangpinoyrecipes.com/dinakdakan-recipe/")// not ok delay loading of application/ld+json
            //loadUrl("https://www.pagkaingpinoytv.com/okoy-kalabasa/#recipe")
            //loadUrl("https://www.cooking-therapy.com/chicken-adobo/")
            //loadUrl("https://www.food.com/recipe/filipino-chicken-adobo-adobong-manok-229484")
            //loadUrl("https://www.kawalingpinoy.com/chicken-adobo/")
            //loadUrl("https://www.internationalcuisine.com/chicken-adobo/")
            //loadUrl("https://www.allrecipes.com/recipe/21014/good-old-fashioned-pancakes/?fbclid=IwAR3qclwSSbUT90qk8edUEdHmNSKAtGEWNvSsntZASTalf0oBeWpPzNaJ4Ow")
            //loadUrl("https://www.knorr.com/ph/recipe-ideas/chicken-adobo.html?gclsrc=aw.ds&gclid=EAIaIQobChMIsJ72y96f8wIVVplmAh22fA0yEAAYASAAEgLTS_D_BwE")//metadata
            //loadUrl("https://www.tasteofhome.com/recipes/filipino-chicken-adobo/")//metadata
            loadUrl("https://www.filipinochow.com/recipes/adobong-manok-chicken-adobo/")//metadata




            addJavascriptInterface(JavascriptBridge(), "JavascriptBridge")
        }
    }


    suspend fun parseRecipe(html:String){

//        val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("BrowseRecipeFragment"))
//        mCoroutineScope.launch {

                val doc = Jsoup.parse(html)
                val scriptElements = doc.select("script[type=\"application/ld+json\"]");

                if(scriptElements !=null){
                    scriptElements.forEach {
                        val jsonStringType = jsonStringType(it.html())

                        when(jsonStringType){
                            JSON_ARRAY_TYPE->{
                                Log.e("array","array")
                                //parseJSONArrayRecipe(scriptContent)
                                parseJSONArrayRecipe(it.html())
                                //return@launch
                            }
                            JSON_OBJECT_TYPE->{
                                Log.e("object","object")
                                if(doStringHasRecipe(it.html())){

                                    val jsonObjectRecipe = JSONObject(it.html())
                                    if(jsonObjectRecipe.has("@type")){
                                            parseJSONObjectRecipe(it.html())
                                            //return@launch

                                    }else if(jsonObjectRecipe.has("@graph")){
                                        parseJSONObjectRecipe(it.html())
                                        //return@launch
                                    }
                                }
                            }
                            UNKNOWN_JSON_TYPE->{
                                Log.e("UNKNOWN_JSON_TYPE","UNKNOWN_JSON_TYPE")
                                //return@launch
                            }
                        }
                    }

                }


                if(mRecipeEntity != null){
                    openViewParsedReciped(mRecipeEntity!!,mIngredients,mRecipeStepEntities)
                    return;
                }

                val typeOfRecipeElement = doc.selectFirst("[typeof=Recipe]")
                val vocabElement = doc.selectFirst("[vocab=http://schema.org/]")

                if(typeOfRecipeElement != null && vocabElement != null){
                    //RDFa schema
                    Log.e("RDFa","RDFa")
                    parseDataForRDFaSchema(typeOfRecipeElement)
                    return
                }

                if(mRecipeEntity != null){
                    openViewParsedReciped(mRecipeEntity!!,mIngredients,mRecipeStepEntities)
                    return
                }

                val itemtypeElement = doc.selectFirst("[itemtype=http://schema.org/Recipe],[itemtype=https://schema.org/Recipe]")

                if(doc.selectFirst("[itemtype=https://schema.org/Recipe],[itemtype=http://schema.org/Recipe]")?.select("[itemscope]") != null){
                    //Microdata schema
                    Log.e("Microdata","Microdata")
                    itemtypeElement?.let{
                        parseDataForMicroData(it)
                    }

                }

                if(mRecipeEntity != null){
                    openViewParsedReciped(mRecipeEntity!!,mIngredients,mRecipeStepEntities)
                    return
                }

                withContext(Main){
                    Toast.makeText(requireContext(),"Recipe not found",Toast.LENGTH_SHORT).show()
                }

                withContext(Main){
                    Toast.makeText(requireContext(),"Something wrong downloading recipe.",Toast.LENGTH_SHORT).show()
                }

//       }
    }
    suspend fun parseDataForRDFaSchema(typeOfRecipeElement: Element){

        val recipeName = typeOfRecipeElement.selectFirst("[property=name]")?.let{it.text()}?:run{""}
        val description = typeOfRecipeElement.selectFirst("[property=description]")?.let{it.text()}?:run{""}
        val serving = getServing(typeOfRecipeElement.selectFirst("[property=recipeYield]")?.let{it.text()}?:run{"0"})
        var preparationTime = typeOfRecipeElement.selectFirst("[property=prepTime]")?.let{it.attr("content")}?:run{"0"}
        val cookTime = typeOfRecipeElement.selectFirst("[property=cookTime]")?.let{it.attr("content") }?:run{ "0"}
        val imageUrl = typeOfRecipeElement.selectFirst("[property=image]")?.let{it.attr("src") }?:run{ ""}

        val preparationMinutesInt = getDuration(preparationTime,"[TH][\\d]+M")
        val preparationHourInt = getDuration(preparationTime,"[\\d]+H")

        val cookMinutesInt = getDuration(cookTime,"[TH][\\d]+M")
        val cookHourInt = getDuration(cookTime,"[\\d]+H")

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mRecipeEntity = RecipeEntity(
            uniqueId = UUID.randomUUID().toString(),
            name = recipeName,
            serving = serving,
            difficulty =  RecipeEntity.DIFFICULTY_NONE,
            preparationHour = preparationHourInt,
            preparationMinutes = preparationMinutesInt,
            cookingHours = cookHourInt,
            cookingMinutes = cookMinutesInt,
            category = "",
            estimatedCost = 0.0,
            description = description,
            imageName="",
            "",
            status = RecipeEntity.NOT_DELETED_STATUS,
            uploaded = RecipeEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime

        )
        mRecipeEntity!!.imageUrl = imageUrl

        typeOfRecipeElement.select("[property=recipeIngredient]")?.let{
            mIngredients = arrayListOf<IngredientEntity>()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            it.forEach {
                var itemUniqueID = UUID.randomUUID().toString()
                val ingredientEntity = IngredientEntity(
                    uniqueId =itemUniqueID,
                    recipeUniqueId="",
                    name=it.text(),
                    status = IngredientEntity.NOT_DELETED_STATUS,
                    uploaded = IngredientEntity.NOT_UPLOADED,
                    created = currentDatetime,
                    modified = currentDatetime

                )
                mIngredients.add(ingredientEntity)
            }
        }

       typeOfRecipeElement.select("[property=recipeInstructions]")?.let{
           mRecipeStepEntities = arrayListOf<RecipeStepEntity>()
           it.forEachIndexed{index, element ->
                var uniqueID = UUID.randomUUID().toString()

                val recipeStep = RecipeStepEntity(
                    uniqueId = uniqueID,
                    recipeUniqueId="",
                    instruction=element.text(),
                    sequence = index + 1,
                    status = RecipeStepEntity.NOT_DELETED_STATUS,
                    uploaded = RecipeStepEntity.NOT_UPLOADED,
                    created = "",
                    modified = ""
                )
                mRecipeStepEntities.add(recipeStep)
            }

        }

        //openViewParsedReciped(recipeEntity!!,ingredientsArrayList!!,recipeStepEntitiesArrayList!!)





    }
    suspend fun parseDataForMicroData(microDataFormatElement: Element){

        createRecipeEntityFromMicroDataFormat(microDataFormatElement)



    }
    suspend fun createRecipeEntityFromMicroDataFormat(microDataFormatElement: Element){
        //val recipeName = microDataFormatElement.selectFirst("[itemprop=name]")
        val recipeName = microDataFormatElement.selectFirst("[itemprop=name]")?.let{
            if(it.text().length > 0){
                it.text()
            }else{
                it.attr("content")
            }
        }?:run{""}
        val description = microDataFormatElement.selectFirst("[itemprop=description]")?.let{
            if(it.text().length> 0){
                it.text()
            }else{
                it.attr("content")
            }
        }?:run{
            microDataFormatElement.selectFirst("[itemprop=summary]")?.let{it.text()}?:run{""}
        }


        val serving = getServing(microDataFormatElement.selectFirst("[itemprop=recipeYield]")?.let{
            if(it.text().length>0){
                it.text()
            }else{
                it.attr("content")
            }
        }?:run{"0"})
        var preparationTime = microDataFormatElement.selectFirst("[itemprop=prepTime]")?.let{timeElement->
            var prepTime:String
            prepTime = timeElement.attr("content")
            if(prepTime.isEmpty()){
                prepTime = timeElement.attr("datetime")
            }
            prepTime
        }?:run{"0"}

        var cookTime = microDataFormatElement.selectFirst("[itemprop=cookTime]")?.let{timeElement->
            var cookingTime:String
            cookingTime = timeElement.attr("content")
            if(cookingTime.isEmpty()){
                cookingTime = timeElement.attr("datetime")
            }
            cookingTime
        }?:run{"0"}

        var imageUrl = microDataFormatElement.selectFirst("[itemprop=image]")?.let{timeElement->
                timeElement.attr("src")
            }?:run{
                microDataFormatElement.selectFirst("[itemprop=photo]")?.let{
                    it.attr("src")
                }
            }
        val preparationMinutesInt = getDuration(preparationTime,"[TH][\\d]+M")
        val preparationHourInt = getDuration(preparationTime,"[\\d]+H")

        val cookMinutesInt = getDuration(cookTime,"[TH][\\d]+M")
        val cookHourInt = getDuration(cookTime,"[\\d]+H")
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mRecipeEntity = RecipeEntity(
            uniqueId = UUID.randomUUID().toString(),
            name = recipeName,
            serving = serving,
            difficulty =  RecipeEntity.DIFFICULTY_NONE,
            preparationHour = preparationHourInt,
            preparationMinutes = preparationMinutesInt,
            cookingHours = cookHourInt,
            cookingMinutes = cookMinutesInt,
            category = "",
            estimatedCost = 0.0,
            description = description,
            imageName="",
            "",
            status = RecipeEntity.NOT_DELETED_STATUS,
            uploaded = RecipeEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime

        )
        mRecipeEntity!!.imageUrl = imageUrl
        mIngredients = getingredientsArrayListFromMicroDataFormat(microDataFormatElement)
        mRecipeStepEntities = getRecipeStepFromMicroDataFormat(microDataFormatElement)

        //openViewParsedReciped(recipeEntity!!,ingredientsArrayList!!,recipeStepEntitiesArrayList!!)




    }
     fun getingredientsArrayListFromMicroDataFormat(microDataFormatElement:Element):ArrayList<IngredientEntity>{
        val ingredients = arrayListOf<IngredientEntity>()
        microDataFormatElement.select("[itemprop=ingredients],[itemprop=recipeIngredient]")?.let{

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            it.forEach {
                var itemUniqueID = UUID.randomUUID().toString()
                val ingredientEntity = IngredientEntity(
                    uniqueId =itemUniqueID,
                    recipeUniqueId="",

                    name=if(it.text().trim().length >0) it.text() else it.attr("content"),
                    status = IngredientEntity.NOT_DELETED_STATUS,
                    uploaded = IngredientEntity.NOT_UPLOADED,
                    created = currentDatetime,
                    modified = currentDatetime

                )
                ingredients.add(ingredientEntity)
            }
            ingredients
        }



       return ingredients
    }
     fun getRecipeStepFromMicroDataFormat(microDataFormatElement:Element):ArrayList<RecipeStepEntity>{
        val recipeStepEntities = arrayListOf<RecipeStepEntity>()

        microDataFormatElement.select("[itemprop=instructions],[itemprop=recipeInstructions]")?.let{
            it.forEachIndexed{index, element ->
                var uniqueID = UUID.randomUUID().toString()

                val recipeStep = RecipeStepEntity(
                    uniqueId = uniqueID,
                    recipeUniqueId="",
                    instruction=if(element.text().trim().length >0) element.text() else element.attr("content"),
                    sequence = index + 1,
                    status = RecipeStepEntity.NOT_DELETED_STATUS,
                    uploaded = RecipeStepEntity.NOT_UPLOADED,
                    created = "",
                    modified = ""
                )
                recipeStepEntities.add(recipeStep)
            }

        }
        if(recipeStepEntities.size <=0){
             microDataFormatElement.select("[itemprop=recipeInstructions]")?.let{
                it.forEachIndexed{index, element ->
                    var uniqueID = UUID.randomUUID().toString()

                    val recipeStep = RecipeStepEntity(
                        uniqueId = uniqueID,
                        recipeUniqueId="",
                        instruction=element.text(),
                        sequence = index + 1,
                        status = RecipeStepEntity.NOT_DELETED_STATUS,
                        uploaded = RecipeStepEntity.NOT_UPLOADED,
                        created = "",
                        modified = ""
                    )
                    recipeStepEntities.add(recipeStep)
                }

            }
        }
        return recipeStepEntities
    }
    suspend fun parseJSONArrayRecipe(stringRecipe:String){
        val jsonArraytRecipe = JSONArray(stringRecipe)

        for (i in 0 until jsonArraytRecipe.length()) {
            val jsonObject = jsonArraytRecipe.getJSONObject(i)

            if(jsonObject.has("@type")){
                val type = jsonObject.getString("@type")
                if(type.equals("Recipe")){

                    mRecipeEntity = createRecipeEntity(jsonObject)

                    val instructions = jsonObject.getJSONArray("recipeInstructions")
                    mIngredients = generateIngredients(jsonObject)
                    mRecipeStepEntities = generateSteps(instructions)

                    //openViewParsedReciped(mRecipeEntity,mIngredients,mRecipeStepEntities)

                    return
                }

            }
            if(jsonObject.has("@graph")) {

                val type = jsonStringType(jsonObject.getString("@graph"))
                when (type) {
                        JSON_ARRAY_TYPE -> {
                            val graphArray = jsonObject.getJSONArray("@graph")
                            for (i in 0 until graphArray.length()) {
                                val jsonObject = graphArray.getJSONObject(i)
                                if(jsonObject.has("@type")){
                                    val type = jsonObject.getString("@type")
                                    if(type.equals("Recipe")){

                                        mRecipeEntity = createRecipeEntity(jsonObject)

                                        val instructions = jsonObject.getJSONArray("recipeInstructions")
                                        mIngredients = generateIngredients(jsonObject)
                                        mRecipeStepEntities = generateSteps(instructions)

                                        //openViewParsedReciped(recipe!!,ingredientsArrayList!!,recipeStepsArrayList!!)

                                        return
                                    }

                                }
                            }
                        }
                        JSON_OBJECT_TYPE -> {

                        }
                    }
                }

        }

    }
    fun doStringHasRecipe(stringRecipe:String):Boolean{
        val jsonObjectRecipe = JSONObject(stringRecipe)
        if(jsonObjectRecipe.has("@type")){

            val jsonType = JSONTokener(jsonObjectRecipe.getString("@type")).nextValue()
            when(jsonType){
                is JSONObject->{

                    return false

                }
                is JSONArray->{
                    return jsonObjectRecipe.getJSONArray("@type")[0].toString().equals("Recipe")

                }
                else->{
                   return jsonObjectRecipe.getString("@type").equals("Recipe")

                }
            }


        }

        val graphJSONArray = jsonObjectRecipe.getJSONArray("@graph")
        for (i in 0 until graphJSONArray.length()) {
            val jsonObject = graphJSONArray.getJSONObject(i)
            if(jsonObject.has("@type")){
                val type = jsonObject.getString("@type")
                if(type.equals("Recipe")){

                    return true
                }
            }

        }
        return false
    }
    suspend fun parseJSONObjectRecipe(stringRecipe:String){
        val jsonObjectRecipe = JSONObject(stringRecipe)
        if(jsonObjectRecipe.has("@type")){
            evaluateJSONObjectRecipe(jsonObjectRecipe)
            return
        }

        val graphJSONArray = jsonObjectRecipe.getJSONArray("@graph")
        for (i in 0 until graphJSONArray.length()) {
            val jsonObject = graphJSONArray.getJSONObject(i)
            if(jsonObject.has("@type")){
                val type = jsonObject.getString("@type")
                if(type.equals("Recipe")){

                    evaluateJSONObjectRecipe(jsonObject)
                }
            }

        }
        Log.e("RECIPE",stringRecipe)

    }
    suspend fun evaluateJSONObjectRecipe(jsonObject:JSONObject){
        mRecipeEntity = createRecipeEntity(jsonObject)
        mIngredients = generateIngredients(jsonObject)
        mRecipeStepEntities= getRecipeStepes(jsonObject)

    }

    suspend fun openViewParsedReciped(recipe:RecipeEntity,ingredientsArrayList:ArrayList<IngredientEntity>,recipeStepsArrayList: ArrayList<RecipeStepEntity>){

        //CoroutineScope(Dispatchers.IO + CoroutineName("BrowseRecipeFragment")).launch {

            recipe?.imageUrl?.let {

                if(it.trim().isEmpty()){
                    return@let;
                }

                try {
                    var imageUrl = it
                    val firstFourCharacter = imageUrl.take(4)

                    if(!firstFourCharacter.equals("http")){
                        imageUrl = "https:${imageUrl}"
                    }
                    val okHttpClient = OkHttpClient.Builder().build()
                    val request = Request.Builder().url(imageUrl).build()
                    val response = okHttpClient.newCall(request).execute()
                    val inputStream: InputStream = response.body!!.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val imagePath = ImageUtil.saveImage(requireContext(),bitmap, GroceryUtil.TEMPORARY_IMAGES_LOCATION,"${ImageUtil.IMAGE_TEMP_NAME}.${ImageUtil.IMAGE_NAME_SUFFIX}")
                    recipe.imageName = "${ImageUtil.IMAGE_TEMP_NAME}.${ImageUtil.IMAGE_NAME_SUFFIX}"
                } catch (e: java.lang.Exception) {
                    recipe.imageName = ""
                }


            }


            withContext(Main){
                val intent = Intent(requireContext(),AddRecipeActivity::class.java)
                intent.putExtra(AddRecipeActivity.ACTION_TAG,AddRecipeActivity.ADD_FROM_BROWSER_ACTION)
                intent.putExtra(AddRecipeActivity.RECIPE_TAG,recipe)
                intent.putParcelableArrayListExtra(AddRecipeActivity.INGREDIENTS_TAG,ingredientsArrayList)
                intent.putParcelableArrayListExtra(AddRecipeActivity.STEPS_TAG,recipeStepsArrayList)
                startActivity(intent)
            }



        //}
    }
    fun createRecipeEntity(jsonObjectRecipe:JSONObject):RecipeEntity?{

        var recipeName:String
        var description:String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recipeName = Html.fromHtml(jsonObjectRecipe.getString("name"),Html.FROM_HTML_MODE_LEGACY).toString()
            description = Html.fromHtml(if(jsonObjectRecipe.has("description"))jsonObjectRecipe.getString("description") else "",Html.FROM_HTML_MODE_LEGACY).toString()
        }else{
            recipeName = Html.fromHtml(jsonObjectRecipe.getString("name")).toString()
            description = Html.fromHtml(jsonObjectRecipe.getString("description")).toString()
        }

        val serving = getServing(getServing(jsonObjectRecipe))
        var preparationTime = if(jsonObjectRecipe.has("prepTime")) jsonObjectRecipe.getString("prepTime") else "0"
        val cookTime = if(jsonObjectRecipe.has("cookTime")) jsonObjectRecipe.getString("cookTime") else "0"
        val category = getCategory(jsonObjectRecipe)
        val imageUrl = getImage(jsonObjectRecipe)

        val preparationMinutesInt = getDuration(preparationTime,"[TH][\\d]+M")
        val preparationHourInt = getDuration(preparationTime,"[\\d]+H")

        val cookMinutesInt = getDuration(cookTime,"[TH][\\d]+M")
        val cookHourInt = getDuration(cookTime,"[\\d]+H")

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val recipeEntity = RecipeEntity(
            uniqueId = UUID.randomUUID().toString(),
            name = recipeName,
            serving = serving,
            difficulty =  RecipeEntity.DIFFICULTY_NONE,
            preparationHour = preparationHourInt,
            preparationMinutes = preparationMinutesInt,
            cookingHours = cookHourInt,
            cookingMinutes = cookMinutesInt,
            category = category,
            estimatedCost = 0.0,
            description = description,
            imageName="",
            "",
            status = RecipeEntity.NOT_DELETED_STATUS,
            uploaded = RecipeEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime

        )
        recipeEntity.imageUrl = imageUrl

        return recipeEntity
    }
    fun getServing(jsonObjectRecipe:JSONObject):String{

        if(!jsonObjectRecipe.has("recipeYield")){
            return "0"
        }

        val jsonType = JSONTokener(jsonObjectRecipe.getString("recipeYield")).nextValue()
        when(jsonType){
            is JSONObject->{
                Log.e("jsonType","JSONObject")
                return "0"
            }
            is JSONArray->{
                return jsonType[0].toString()

            }
            else->{
                return jsonObjectRecipe.getString("recipeYield")
            }
        }
    }
    fun getImage(jsonObjectRecipe:JSONObject):String{
        if(!jsonObjectRecipe.has("image")){
            return ""
        }
        val jsonType = JSONTokener(jsonObjectRecipe.getString("image")).nextValue()
        when(jsonType){
            is JSONObject->{
                val imageUrl =jsonType.getString("url")
                val firstCharacter = imageUrl.first()
                val lastCharacter = imageUrl.last()

                if(firstCharacter.equals("\"")){
                    imageUrl.drop(0)
                }
                if(lastCharacter.equals("\"")){
                    imageUrl.drop(imageUrl.length - 1)
                }

                return jsonType.getString("url")

            }
            is JSONArray->{
                return jsonType[0].toString()

            }
            is String ->{
                return jsonObjectRecipe.getString("image")
            }
            else->{
                return ""
            }
        }
    }
    fun getCategory(jsonObjectRecipe:JSONObject):String{

        if(!jsonObjectRecipe.has("recipeCategory")){
            return ""
        }
        if(jsonObjectRecipe.getString("recipeCategory").isEmpty()){
            return ""
        }

        val jsonType = JSONTokener(jsonObjectRecipe.getString("recipeCategory")).nextValue()
        when(jsonType){
            is JSONObject->{
                return ""

            }
            is JSONArray->{
                return if(jsonType.length() >0) jsonType[0].toString() else ""

            }
            else->{
                return jsonObjectRecipe.getString("recipeCategory")
            }
        }
    }
    fun getRecipeStepes(jsonObjectRecipe:JSONObject):ArrayList<RecipeStepEntity>{

        if(!jsonObjectRecipe.has("recipeInstructions") || !JSONTokener(jsonObjectRecipe.getString("recipeInstructions")).more()){
            return arrayListOf()
        }


        val jsonType = JSONTokener(jsonObjectRecipe.getString("recipeInstructions")).nextValue()
        when(jsonType){
            is JSONObject->{
                Log.e("jsonType","JSONObject")
                return arrayListOf()
            }
            is JSONArray->{
                val instructions = jsonObjectRecipe.getJSONArray("recipeInstructions")
                return generateSteps(instructions)

            }
            else->{
                var uniqueID = UUID.randomUUID().toString()
                var step:String
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    step = Html.fromHtml(jsonObjectRecipe.getString("recipeInstructions"),Html.FROM_HTML_MODE_LEGACY).toString()
                }else{
                    step = Html.fromHtml(jsonObjectRecipe.getString("recipeInstructions")).toString()
                }
                val recipeStep = RecipeStepEntity(
                    uniqueId = uniqueID,
                    recipeUniqueId="",
                    instruction=step,
                    sequence = 1,
                    status = RecipeStepEntity.NOT_DELETED_STATUS,
                    uploaded = RecipeStepEntity.NOT_UPLOADED,
                    created = "",
                    modified = ""
                )
                val recipeSteps = ArrayList<RecipeStepEntity>()
                recipeSteps.add(recipeStep)

                return recipeSteps
            }
        }

    }
    fun generateIngredients(jsonObjectRecipe:JSONObject): ArrayList<IngredientEntity>{
        var ingredients = arrayListOf<IngredientEntity>()

        if(jsonObjectRecipe["recipeIngredient"] is String){
            var itemUniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            val ingredientEntity = IngredientEntity(
                uniqueId =itemUniqueID,
                recipeUniqueId="",
                name=jsonObjectRecipe.getString("recipeIngredient"),
                status = IngredientEntity.NOT_DELETED_STATUS,
                uploaded = IngredientEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime

            )
            ingredients.add(ingredientEntity)
            return ingredients
        }

        val ingredientsJSONArray = jsonObjectRecipe.getJSONArray("recipeIngredient")
        for (i in 0 until ingredientsJSONArray.length()) {
            val ingredient = ingredientsJSONArray.getString(i).replace("<[a-zA-Z0-9\\\$\\s\\/\\/:.\"=\\-_]+>".toRegex(),"")
            var itemUniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            val ingredientEntity = IngredientEntity(
                uniqueId =itemUniqueID,
                recipeUniqueId="",
                name=ingredient,
                status = IngredientEntity.NOT_DELETED_STATUS,
                uploaded = IngredientEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime

            )
            ingredients.add(ingredientEntity)

        }
       return ingredients
    }
    fun generateSteps(instructionsJSONArray:JSONArray):ArrayList<RecipeStepEntity>{

        if(instructionsJSONArray.length() >0 && jsonStringType(instructionsJSONArray[0].toString()) != JSON_ARRAY_TYPE && jsonStringType(instructionsJSONArray[0].toString()) != JSON_OBJECT_TYPE){
            return generateSteps___(instructionsJSONArray)
        }


        if(instructionsJSONArray.length() >0 && jsonStringType(instructionsJSONArray[0].toString()) == JSON_ARRAY_TYPE){
            return generateSteps__(instructionsJSONArray.getJSONArray(0))
        }


        if(instructionsJSONArray.length() >0 && !instructionsJSONArray.getJSONObject(0).has("text") && !(instructionsJSONArray[0] is String)){
           return generateSteps_(instructionsJSONArray)
        }

        var recipeStepEntities = arrayListOf<RecipeStepEntity>()
        for (i in 0 until instructionsJSONArray.length()) {

            if(!instructionsJSONArray.getJSONObject(i).has("text") && !(instructionsJSONArray[i] is String)){
                break
            }
            var step = if(instructionsJSONArray[i] is String) instructionsJSONArray[i].toString() else instructionsJSONArray.getJSONObject(i).getString("text")
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                step = Html.fromHtml(step,Html.FROM_HTML_MODE_LEGACY).toString()
            }else{
                step = Html.fromHtml(step).toString()
            }

            var uniqueID = UUID.randomUUID().toString()

            val recipeStep = RecipeStepEntity(
                uniqueId = uniqueID,
                recipeUniqueId="",
                instruction=step,
                sequence = i + 1,
                status = RecipeStepEntity.NOT_DELETED_STATUS,
                uploaded = RecipeStepEntity.NOT_UPLOADED,
                created = "",
                modified = ""
            )
            recipeStepEntities.add(recipeStep)
        }
        return recipeStepEntities
    }
    fun generateSteps_(instructionsJSONArray:JSONArray):ArrayList<RecipeStepEntity>{
        var recipeStepEntities = arrayListOf<RecipeStepEntity>()

        for (i in 0 until instructionsJSONArray.length()) {
            val jsonData = instructionsJSONArray.getJSONObject(i)
            if(jsonData.has("itemListElement")){
                val jsonType = JSONTokener(jsonData.getString("itemListElement")).nextValue()
                when(jsonType){
                    is JSONObject->{


                    }
                    is JSONArray->{
                        val itemListElement =  jsonData.getJSONArray("itemListElement")
                        for (i in 0 until itemListElement.length()) {
                            var step =  itemListElement.getJSONObject(i).getString("text")
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                step = Html.fromHtml(step,Html.FROM_HTML_MODE_LEGACY).toString()
                            }else{
                                step = Html.fromHtml(step).toString()
                            }

                            var uniqueID = UUID.randomUUID().toString()

                            val recipeStep = RecipeStepEntity(
                                uniqueId = uniqueID,
                                recipeUniqueId="",
                                instruction=step,
                                sequence = i + 1,
                                status = RecipeStepEntity.NOT_DELETED_STATUS,
                                uploaded = RecipeStepEntity.NOT_UPLOADED,
                                created = "",
                                modified = ""
                            )
                            recipeStepEntities.add(recipeStep)
                        }
                    }

                }
            }
        }
        return recipeStepEntities

    }
    fun generateSteps__(instructionsJSONArray:JSONArray):ArrayList<RecipeStepEntity>{
        var recipeStepEntities = arrayListOf<RecipeStepEntity>()

        for (i in 0 until instructionsJSONArray.length()) {
            val jsonData = instructionsJSONArray.getJSONObject(i)
            if(jsonData.has("itemListElement")){
                val jsonType = JSONTokener(jsonData.getString("itemListElement")).nextValue()
                when(jsonType){
                    is JSONObject->{


                    }
                    is JSONArray->{
                        val itemListElement =  jsonData.getJSONArray("itemListElement")
                        for (i in 0 until itemListElement.length()) {
                            var step =  itemListElement.getJSONObject(i).getString("text")
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                step = Html.fromHtml(step,Html.FROM_HTML_MODE_LEGACY).toString()
                            }else{
                                step = Html.fromHtml(step).toString()
                            }

                            var uniqueID = UUID.randomUUID().toString()

                            val recipeStep = RecipeStepEntity(
                                uniqueId = uniqueID,
                                recipeUniqueId="",
                                instruction=step,
                                sequence = i + 1,
                                status = RecipeStepEntity.NOT_DELETED_STATUS,
                                uploaded = RecipeStepEntity.NOT_UPLOADED,
                                created = "",
                                modified = ""
                            )
                            recipeStepEntities.add(recipeStep)
                        }
                    }

                }
            }else if(jsonData.has("text")){
                var step =  jsonData.getString("text")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    step = Html.fromHtml(step,Html.FROM_HTML_MODE_LEGACY).toString()
                }else{
                    step = Html.fromHtml(step).toString()
                }

                var uniqueID = UUID.randomUUID().toString()

                val recipeStep = RecipeStepEntity(
                    uniqueId = uniqueID,
                    recipeUniqueId="",
                    instruction=step,
                    sequence = i + 1,
                    status = RecipeStepEntity.NOT_DELETED_STATUS,
                    uploaded = RecipeStepEntity.NOT_UPLOADED,
                    created = "",
                    modified = ""
                )
                recipeStepEntities.add(recipeStep)
            }
        }
        return recipeStepEntities

    }
    fun generateSteps___(instructionsJSONArray:JSONArray):ArrayList<RecipeStepEntity>{
        var recipeStepEntities = arrayListOf<RecipeStepEntity>()
        for (i in 0 until instructionsJSONArray.length()) {
             var step =  instructionsJSONArray[i].toString()
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                step = Html.fromHtml(step,Html.FROM_HTML_MODE_LEGACY).toString()
             }else{
                step = Html.fromHtml(step).toString()
             }

             var uniqueID = UUID.randomUUID().toString()

              val recipeStep = RecipeStepEntity(
                uniqueId = uniqueID,
                recipeUniqueId="",
                instruction=step,
                sequence = i + 1,
                status = RecipeStepEntity.NOT_DELETED_STATUS,
                uploaded = RecipeStepEntity.NOT_UPLOADED,
                created = "",
                modified = ""
                )
              recipeStepEntities.add(recipeStep)
         }

        return recipeStepEntities

    }
    fun getDuration(isoDurationString:String,regex:String):Int{
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(isoDurationString)
        if(matcher.find()){
            return matcher.group(0).replace("[a-zA-Z]".toRegex(),"").toInt()
        }
        return 0
    }
    fun getServing(serving:String = "0"):Int{
        try {
            val regex = "^[0-9|Serves|\\s]+"
            val pattern: Pattern = Pattern.compile(regex)
            val matcher: Matcher = pattern.matcher(serving)
            if(matcher.find()){
                return matcher.group(0).replace("[a-zA-Z\\s]".toRegex(),"").toInt()
            }
        } catch (e: Exception) {
            return 0
        }
        return 0
    }
    fun jsonStringType(string:String):Int{

        if(string.length <=0){
            return UNKNOWN_JSON_TYPE
        }
        try {
            // some code
            JSONArray(string)
            return JSON_ARRAY_TYPE
        } catch (e: Exception) {

            try {

                JSONObject(string)
                return JSON_OBJECT_TYPE
            }catch (e:Exception){
                return UNKNOWN_JSON_TYPE
            }


        }
        return UNKNOWN_JSON_TYPE
    }
    companion object {
        const val UNKNOWN_JSON_TYPE = 0
        const val JSON_ARRAY_TYPE =1
        const val JSON_OBJECT_TYPE = 2
        @JvmStatic fun newInstance(param1: String, param2: String) =
            BrowseRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class JavascriptBridge{
        @JavascriptInterface
        fun getHtmlAsString(html:String){


            mProgressDialogFragment.show(requireActivity().supportFragmentManager,"ProgressDialogFragment")
            mProgressDialogFragment.isCancelable = false;

            mRecipeEntity = null
            mIngredients = arrayListOf<IngredientEntity>()
            mRecipeStepEntities = arrayListOf<RecipeStepEntity>()
            val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("BrowseRecipeFragment"))
            mCoroutineScope.launch {
                try {
                    parseRecipe(html)
                    mProgressDialogFragment.dismiss()
                }catch (e:Exception){
                    withContext(Main){
                        mProgressDialogFragment.dismiss()
                        Toast.makeText(requireContext(),"Something wrong downloading recipe.",Toast.LENGTH_SHORT).show()
                    }
                }


            }

        }

    }


}