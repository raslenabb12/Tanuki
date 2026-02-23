package com.youme.tanuki

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class manga_search: AppCompatActivity() {
    private val viewModel: MangaViewModel by viewModels()
    private lateinit var searchEditText: TextInputEditText
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var sortChip: Chip
    private lateinit var genreChip: Chip
    private lateinit var statusChip: Chip
    private lateinit var formatChip: Chip
    private lateinit var countryChip: Chip
    private lateinit var tagship: Chip
    private var tags : List<String>?=null
    private var repository: MangaRepository=MangaRepository()
    private lateinit var activeFiltersChipGroup: ChipGroup
    private val loadStateAdapter = MangaLoadStateAdapter()
    private lateinit var mangaRecyclerView: RecyclerView
    private val mangaAdapter: MangaPagingAdapter by lazy { MangaPagingAdapter(::onMangaItemClick) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_laoyut)
        setupLoadStateHandling()
        initializeViews()
        setupSearch()
        setupFilterChips()
        setupRecyclerView()
        observeViewModel()
        val genre: String? = intent.getStringExtra("genre")
        genre?.let {
            viewModel.setGenres(listOf(it))
        }
        tags= listOf("4-koma","Achromatic","Achronological Order","Acrobatics","Acting","Adoption","Advertisement","Afterlife","Age Gap","Age Regression","Agender","Agriculture","Ahegao","Airsoft","Alchemy","Aliens","Alternate Universe","American Football","Amnesia","Amputation","Anachronism","Anal Sex","Ancient China","Angels","Animals","Anthology","Anthropomorphism","Anti-Hero","Archery","Armpits","Aromantic","Arranged Marriage","Artificial Intelligence","Asexual","Ashikoki","Asphyxiation","Assassins","Astronomy","Athletics","Augmented Reality","Autobiographical","Aviation","Badminton","Band","Bar","Baseball","Basketball","Battle Royale","Biographical","Bisexual","Blackmail","Board Game","Boarding School","Body Horror","Body Swapping","Bondage","Boobjob","Bowling","Boxing","Boys' Love","Bullying","Butler","Calligraphy","Camping","Cannibalism","Card Battle","Cars","Centaur","Cervix Penetration","CGI","Cheating","Cheerleading","Chibi","Chimera","Chuunibyou","Circus","Class Struggle","Classic Literature","Classical Music","Clone","Coastal","College","Coming of Age","Conspiracy","Cosmic Horror","Cosplay","Cowboys","Crime","Criminal Organization","Crossdressing","Crossover","Cult","Cultivation","Cumflation","Cunnilingus","Curses","Cute Boys Doing Cute Things","Cute Girls Doing Cute Things","Cyberpunk","Cyborg","Cycling","Dancing","Death Game","Deepthroat","Defloration","Delinquents","Demons","Denpa","Desert","Detective","DILF","Dinosaurs","Disability","Dissociative Identities","Double Penetration","Dragons","Drawing","Drugs","Dullahan","Dungeon","Dystopian","E-Sports","Eco-Horror","Economics","Educational","Elderly Protagonist","Elf","Ensemble Cast","Environmental","Episodic","Ero Guro","Erotic Piercings","Espionage","Estranged Family","Exhibitionism","Exorcism","Facial","Fairy","Fairy Tale","Fake Relationship","Family Life","Fashion","Feet","Fellatio","Female Harem","Female Protagonist","Femboy","Femdom","Fencing","Filmmaking","Firefighters","Fishing","Fisting","Fitness","Flash","Flat Chest","Food","Football","Foreign","Found Family","Fugitive","Full CGI","Full Color","Futanari","Gambling","Gangs","Gender Bending","Ghost","Go","Goblin","Gods","Golf","Gore","Group Sex","Guns","Gyaru","Hair Pulling","Handball","Handjob","Henshin","Heterosexual","Hikikomori","Hip-hop Music","Historical","Homeless","Horticulture","Human Pet","Hypersexuality","Ice Skating","Idol","Incest","Inn","Inseki","Irrumatio","Isekai","Iyashikei","Jazz Music","Josei","Judo","Kaiju","Karuta","Kemonomimi","Kids","Kingdom Management","Konbini","Kuudere","Lacrosse","Lactation","Language Barrier","Large Breasts","LGBTQ+ Themes","Lost Civilization","Love Triangle","Mafia","Magic","Mahjong","Maids","Makeup","Male Harem","Male Pregnancy","Male Protagonist","Marriage","Martial Arts","Masochism","Masturbation","Matchmaking","Mating Press","Matriarchy","Medicine","Memory Manipulation","Mermaid","Meta","Metal Music","MILF","Military","Mixed Gender Harem","Monster Boy","Monster Girl","Mopeds","Motorcycles","Mountaineering","Musical Theater","Mythology","Nakadashi","Natural Disaster","Necromancy","Nekomimi","Netorare","Netorase","Netori","Ninja","No Dialogue","Noir","Non-fiction","Nudity","Nun","Office","Office Lady","Oiran","Ojou-sama","Omegaverse","Orphan","Otaku Culture","Outdoor","Pandemic","Parenthood","Parkour","Parody","Pet Play","Philosophy","Photography","Pirates","Poker","Police","Politics","Polyamorous","Post-Apocalyptic","POV","Pregnant","Primarily Adult Cast","Primarily Animal Cast","Primarily Child Cast","Primarily Female Cast","Primarily Male Cast","Primarily Teen Cast","Prison","Prostitution","Proxy Battle","Psychosexual","Public Sex","Puppetry","Rakugo","Rape","Real Robot","Rehabilitation","Reincarnation","Religion","Restaurant","Revenge","Rimjob","Robots","Rock Music","Rotoscoping","Royal Affairs","Rugby","Rural","Sadism","Samurai","Satire","Scat","School","School Club","Scissoring","Scuba Diving","Seinen","Sex Toys","Shapeshifting","Ships","Shogi","Shoujo","Shounen","Shrine Maiden","Skateboarding","Skeleton","Slapstick","Slavery","Snowscape","Software Development","Space","Space Opera","Spearplay","Squirting","Steampunk","Stop Motion","Succubus","Suicide","Sumata","Sumo","Super Power","Super Robot","Superhero","Surfing","Surreal Comedy","Survival","Sweat","Swimming","Swordplay","Table Tennis","Tanks","Tanned Skin","Teacher","Teens' Love","Tennis","Tentacles","Terrorism","Threesome","Time Loop","Time Manipulation","Time Skip","Tokusatsu","Tomboy","Torture","Tragedy","Trains","Transgender","Travel","Triads","Tsundere","Twins","Unrequited Love","Urban","Urban Fantasy","Vampire","Veterinarian","Video Games","Vikings","Villainess","Virginity","Virtual World","Vocal synth","Volleyball","Vore","Voyeur","VTuber","War","Watersports","Werewolf","Witch","Work","Wrestling","Writing","Wuxia","Yakuza","Yandere","Youkai","Yuri","Zombie","Zoophilia")
//        lifecycleScope.launch {
//            repository.getAlltags().onSuccess {
//                tags=it
//            }
//        }
    }
    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        genreChip = findViewById(R.id.genreChip)
        statusChip = findViewById(R.id.statusChip)
        formatChip = findViewById(R.id.formatChip)
        countryChip = findViewById(R.id.countryship)
        tagship = findViewById(R.id.tagship)
        activeFiltersChipGroup = findViewById(R.id.activeFiltersChipGroup)
        mangaRecyclerView = findViewById(R.id.mangaRecyclerView)
    }


    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString())
            }
        })
    }
    private fun onMangaItemClick(mangaItem: Manga, imageView: ImageView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            imageView,
            "manga_cover"
        )
        val intent = Intent(this, MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
            putExtra("imageurl", mangaItem.coverImage.large)
            putExtra("mangaid", mangaItem.id)
        }
        startActivity(intent, options.toBundle())
    }
    private fun setupFilterChips() {
        genreChip.setOnClickListener { showFilterDialog() }
        statusChip.setOnClickListener { showStatusDialog() }
        formatChip.setOnClickListener { showFormatDialog() }
        countryChip.setOnClickListener { showCountryDialog() }
        tagship.setOnClickListener { showTagDialog() }
    }
    private fun showCountryDialog() {
        val items = listOf("Japan","South Korea","China")
        val adapter = ArrayAdapter(this, R.layout.dialog_item, items)
        MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
            .setTitle("Select Country")
            .setAdapter(adapter) { _, index ->
                viewModel.setCountryoforigin(CountryofOrigin.entries[index])
            }
            .show()
    }
    private fun showStatusDialog() {
        val items = MangaStatus.entries.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.dialog_item, items)
        MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
            .setTitle("Select Status")
            .setAdapter(adapter) { _, index ->
                viewModel.setStatus(MangaStatus.entries[index])
            }
            .show()
    }

    private fun showFormatDialog() {
        val items = MangaFormat.entries.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.dialog_item, items)
        MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
            .setTitle("Select Format")
            .setAdapter(adapter) { _, index ->
                viewModel.setFormat(MangaFormat.entries[index])
            }
            .show()
    }
    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this@manga_search, 3).apply {
            initialPrefetchItemCount=20
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == mangaAdapter.itemCount) 3 else 1
                }
            }
        }
        mangaRecyclerView.apply {
            layoutManager =gridLayoutManager
            adapter = mangaAdapter.withLoadStateFooter(
                footer = loadStateAdapter
            )
        }
    }
    private fun showTagDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_options, null)
        val genreChipGroup = dialogView.findViewById<ChipGroup>(R.id.genreChipGroup)
        val dialog = MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
            .setTitle("Tags")
            .setView(dialogView)
            .create()
        dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
            val selectedGenres = genreChipGroup.checkedChipIds.mapNotNull { id ->
                genreChipGroup.findViewById<Chip>(id)?.text?.toString()
            }
            viewModel.setTags(selectedGenres)
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.button3).setOnClickListener {
            dialog.dismiss()
        }
        tags?.forEach { genre ->
            val chip = Chip(dialog.context).apply {
                text = genre
                chipBackgroundColor= ColorStateList.valueOf(Color.DKGRAY)
                setTextColor(Color.WHITE)
                setOnCheckedChangeListener { _, isChecked ->
                    chipBackgroundColor=ColorStateList.valueOf(if (isChecked) Color.parseColor("#FFDD55") else Color.DKGRAY)
                }
                isCheckable = true
            }
            genreChipGroup.addView(chip)
        }
        dialog.show()
    }
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_options, null)
        val genreChipGroup = dialogView.findViewById<ChipGroup>(R.id.genreChipGroup)
        val dialog = MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
            .setTitle("Genres")
            .setView(dialogView)
            .create()
        dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
            val selectedGenres = genreChipGroup.checkedChipIds.mapNotNull { id ->
                genreChipGroup.findViewById<Chip>(id)?.text?.toString()
            }
            viewModel.setGenres(selectedGenres)
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.button3).setOnClickListener {
            dialog.dismiss()
        }
        val genres = listOf("Action",
            "Adventure",
            "Comedy",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Hentai",
            "Horror",
            "Mahou Shoujo",
            "Mecha",
            "Music",
            "Mystery",
            "Psychological",
            "Romance",
            "Sci-Fi",
            "Slice of Life",
            "Sports",
            "Supernatural",
            "Thriller")
        genres.forEach { genre ->
            val chip = Chip(dialog.context).apply {
                text = genre
                chipBackgroundColor= ColorStateList.valueOf(Color.DKGRAY)
                setTextColor(Color.WHITE)
                setOnCheckedChangeListener { _, isChecked ->
                    chipBackgroundColor=ColorStateList.valueOf(if (isChecked) Color.parseColor("#FFDD55") else Color.DKGRAY)
                }
                isCheckable = true
            }
            genreChipGroup.addView(chip)
        }
        dialog.show()
    }
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.mangaFlow.collectLatest { pagingData ->
                mangaAdapter.submitData(pagingData)
            }
        }
        findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.mangaFlow.collectLatest { pagingData ->
                    mangaAdapter.submitData(pagingData)

                }
            }
            findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing=false
        }
        lifecycleScope.launch {
            viewModel.searchParameters.collectLatest { params ->
                updateActiveFilters(params)
            }
        }
    }

    private fun updateActiveFilters(params: MangaSearchParameters) {
        activeFiltersChipGroup.removeAllViews()
        params.genres.forEach { genre ->
            addActiveFilterChip(genre) {
                viewModel.setGenres(params.genres - genre)
            }
        }
        params.tags.forEach { tag ->
            addActiveFilterChip(tag) {
                viewModel.setTags(params.tags - tag)
            }
        }
        params.countryOfOrigin?.let { country ->
            addActiveFilterChip("country: ${country.name}") {
                viewModel.setCountryoforigin(null)
            }
        }
        params.status?.let { status ->
            addActiveFilterChip("Status: ${status.name}") {
                viewModel.setStatus(null)
            }
        }
        params.format?.let { format ->
            addActiveFilterChip("Format: ${format.name}") {
                viewModel.setFormat(null)
            }
        }
    }
    private fun addActiveFilterChip(text: String, onClose: () -> Unit) {
        val chip = Chip(this).apply {
            this.text = text
            this.chipBackgroundColor= ColorStateList.valueOf(Color.DKGRAY)
            this.setTextColor(ColorStateList.valueOf(Color.WHITE))
            isCloseIconVisible = true
            this.closeIconTint=ColorStateList.valueOf(Color.WHITE)
            setOnCloseIconClickListener { onClose() }
        }
        activeFiltersChipGroup.addView(chip)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun setupLoadStateHandling() {
        this@manga_search.lifecycleScope.launch {
            this@manga_search.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mangaAdapter.loadStateFlow.collectLatest { loadStates ->
                    findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).isRefreshing = loadStates.refresh is LoadState.Loading
                    if (loadStates.refresh is LoadState.Loading) {
                        loadStateAdapter.loadState = LoadState.Loading
                    }
                    val errorState = loadStates.refresh as? LoadState.Error
                        ?: loadStates.append as? LoadState.Error
                        ?: loadStates.prepend as? LoadState.Error
                    errorState?.error?.let { throwable ->
                        //showError(throwable.message ?: "")
                    }
                }
            }
        }
    }
    private fun onMangaItemClick(mangaItem: Manga, cardView: CardView) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            cardView,
            "manga_cover"
        )
        val intent = Intent(this, MangaInfo::class.java).apply {
            putExtra("Charactername", mangaItem.title.english ?: mangaItem.title.romaji)
            putExtra("imageurl", mangaItem.coverImage.large)
            putExtra("mangaid", mangaItem.id)
        }
        startActivity(intent, options.toBundle())
    }
}