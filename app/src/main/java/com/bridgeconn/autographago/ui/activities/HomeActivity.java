package com.bridgeconn.autographago.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bridgeconn.autographago.R;
import com.bridgeconn.autographago.models.BookIdModel;
import com.bridgeconn.autographago.models.LanguageModel;
import com.bridgeconn.autographago.ormutils.AutographaRepository;
import com.bridgeconn.autographago.ormutils.Mapper;
import com.bridgeconn.autographago.ormutils.Specification;
import com.bridgeconn.autographago.ui.adapters.BookAdapter;
import com.bridgeconn.autographago.utils.Constants;
import com.bridgeconn.autographago.utils.SharedPrefs;
import com.bridgeconn.autographago.utils.UtilFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
//        AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener {

    private TextView mToolbarTitle;
    private ImageView mContinueRead;
    private ImageView mNotesView;
    private ImageView mBookmarkView;
    private ImageView mHighlightsView;
    private ImageView mSearchView;
    private ImageView mHistoryView;
    private ImageView mSettingsView;
    private RecyclerView mRecyclerView;
    private BookAdapter mAdapter;
    private ArrayList<BookIdModel> mBookModelArrayList = new ArrayList<>();
    //    private AppCompatSpinner mSpinner;
//    private List<SpinnerModel> categoriesList = new ArrayList<>();
//    private SpinnerAdapter spinnerAdapter;
    private String languageCode, languageName, versionCode;
    private Constants.ReadingMode mReadingMode;
    private Constants.FontSize mFontSize;
    private RadioGroup sectionGroupView;
    private RadioButton oldSection, newSection;
    private LinearLayoutManager mLayoutManager;
    private TextView mTvLanguageVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(SharedPrefs.getFontSize().getResId(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        UtilFunctions.applyReadingMode();

        mReadingMode = SharedPrefs.getReadingMode();
        mFontSize = SharedPrefs.getFontSize();

        languageCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, "ENG");
        languageName = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_NAME, "English");
        versionCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, Constants.VersionCodes.ULB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setContentInsetStartWithNavigation(0);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mContinueRead = (ImageView) findViewById(R.id.iv_continue_reading);
        mNotesView = (ImageView) findViewById(R.id.iv_notes);
        mBookmarkView = (ImageView) findViewById(R.id.iv_bookmark);
        mHighlightsView = (ImageView) findViewById(R.id.iv_highlights);
        mSearchView = (ImageView) findViewById(R.id.iv_search);
        mHistoryView = (ImageView) findViewById(R.id.iv_history);
        mSettingsView = (ImageView) findViewById(R.id.iv_settings);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_books);
//        mSpinner = (AppCompatSpinner) findViewById(R.id.spinner);
        sectionGroupView = (RadioGroup) findViewById(R.id.section_group);
        oldSection = (RadioButton) findViewById(R.id.oldSection);
        newSection = (RadioButton) findViewById(R.id.newSection);
        mTvLanguageVersion = (TextView) findViewById(R.id.tv_language_version);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new BookAdapter(this, mBookModelArrayList);
        mRecyclerView.setAdapter(mAdapter);

//        mSpinner.setOnItemSelectedListener(this);
        mToolbarTitle.setOnClickListener(this);
        mContinueRead.setOnClickListener(this);
        mNotesView.setOnClickListener(this);
        mBookmarkView.setOnClickListener(this);
        mHighlightsView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
        mHistoryView.setOnClickListener(this);
        mSettingsView.setOnClickListener(this);
        sectionGroupView.setOnCheckedChangeListener(this);
        mTvLanguageVersion.setOnClickListener(this);

//        spinnerAdapter = new SpinnerAdapter(this, categoriesList);
//        mSpinner.setAdapter(spinnerAdapter);

//        getCategories();

        mTvLanguageVersion.setText(languageName + " " + versionCode);

//        SpinnerModel compareModel = new SpinnerModel();
//        compareModel.setLanguageName(languageName);
//        compareModel.setVersionCode(versionCode);
//        compareModel.setLanguageCode(languageCode);

//        int spinnerPosition = findIndex(compareModel);
//        mSpinner.setSelection(spinnerPosition);

        getAllBooks();

        registerReceiver(onParsingComplete, new IntentFilter(Constants.ACTION.PARSE_COMPLETE));

        UtilFunctions.queueArchivesForUnzipping(this);
        UtilFunctions.queueDirectoriesForParsing(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                sectionGroupView.setOnCheckedChangeListener(null);
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == -1) {
                    sectionGroupView.setOnCheckedChangeListener(HomeActivity.this);
                    return;
                }
                if (mBookModelArrayList.get(firstVisibleItem).getBookNumber() > 40) {
                    newSection.setChecked(true);
                } else {
                    oldSection.setChecked(true);
                }
                sectionGroupView.setOnCheckedChangeListener(HomeActivity.this);
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.section_group: {
                switch (checkedId) {
                    case R.id.oldSection: {
                        mLayoutManager.scrollToPositionWithOffset(0, 0);
                        break;
                    }
                    case R.id.newSection: {
                        int position = 0;
                        for (int i=0; i<mBookModelArrayList.size(); i++) {
                            if (mBookModelArrayList.get(i).getBookNumber() > 40) {
                                position = i;
                                break;
                            }
                        }
                        mLayoutManager.scrollToPositionWithOffset(position, 0);
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public int getSelectedSpinnerPosition() {
        return 0;//mSpinner.getSelectedItemPosition();
    }

//    public int findIndex(SpinnerModel model) {
//        for (int i=0; i<categoriesList.size(); i++) {
//            SpinnerModel spinnerModel = categoriesList.get(i);
//            if (spinnerModel.getVersionCode().equalsIgnoreCase(model.getVersionCode()) &&
//                    spinnerModel.getLanguageName().equalsIgnoreCase(model.getLanguageName()) &&
//                    spinnerModel.getLanguageCode().equalsIgnoreCase(model.getLanguageCode())) {
//                return i;
//            }
//        }
//        return -1;
//    }

//    private void getCategories() {
//        final Realm realm = Realm.getDefaultInstance();
//        categoriesList.clear();
//        ArrayList<LanguageModel> languageModels = query(realm, new AllSpecifications.AllLanguages(), new AllMappers.LanguageMapper());
//        for (LanguageModel languageModel : languageModels) {
//            for (VersionModel versionModel : languageModel.getVersionModels()) {
//                SpinnerModel spinnerModel = new SpinnerModel();
//                spinnerModel.setLanguageCode(languageModel.getLanguageCode());
//                spinnerModel.setVersionCode(versionModel.getVersionCode());
//                spinnerModel.setLanguageName(languageModel.getLanguageName());
//                categoriesList.add(spinnerModel);
//            }
//        }
//        SpinnerModel spinnerModelULB = new SpinnerModel();
//        spinnerModelULB.setLanguageCode("ENG");
//        spinnerModelULB.setLanguageName("English");
//        spinnerModelULB.setVersionCode(Constants.VersionCodes.ULB);
//        boolean presentULB = categoriesList.remove(spinnerModelULB);
//        if (presentULB) {
//            categoriesList.add(0, spinnerModelULB);
//        }
//        SpinnerModel spinnerModelUDB = new SpinnerModel();
//        spinnerModelUDB.setLanguageCode("ENG");
//        spinnerModelUDB.setLanguageName("English");
//        spinnerModelUDB.setVersionCode(Constants.VersionCodes.UDB);
//        boolean presentUDB = categoriesList.remove(spinnerModelUDB);
//        if (presentUDB) {
//            if (presentULB) {
//                categoriesList.add(1, spinnerModelUDB);
//            } else {
//                categoriesList.add(0, spinnerModelUDB);
//            }
//        }
//        SpinnerModel importModel = new SpinnerModel();
//        importModel.setLanguageName(getResources().getString(R.string.download_more));
//        importModel.setLanguageCode("");
//        importModel.setVersionCode("");
//        categoriesList.add(importModel);
//
//        realm.close();
//        spinnerAdapter.notifyDataSetChanged();
//    }

    public ArrayList<LanguageModel> query(Realm realm, Specification<LanguageModel> specification, Mapper<LanguageModel, LanguageModel> mapper) {
        RealmResults<LanguageModel> realmResults = specification.generateResults(realm);
        ArrayList<LanguageModel> resultsToReturn = new ArrayList<>();
        for (LanguageModel result : realmResults) {
            resultsToReturn.add(mapper.map(result));
        }
        return resultsToReturn;
    }

//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        if (position == categoriesList.size() - 1) {
//            Intent settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
//            settingsIntent.putExtra(Constants.Keys.IMPORT_BIBLE, true);
//            startActivityForResult(settingsIntent, Constants.RequestCodes.SETTINGS);
//
//            SpinnerModel compareModel = new SpinnerModel();
//            compareModel.setLanguageName(languageName);
//            compareModel.setVersionCode(versionCode);
//            compareModel.setLanguageCode(languageCode);
//
//            int spinnerPosition = findIndex(compareModel);
//            mSpinner.setSelection(spinnerPosition);
//        } else {
//            SpinnerModel spinnerModel = categoriesList.get(position);//(SpinnerModel) parent.getItemAtPosition(position);
//            if (spinnerModel.getLanguageCode().equalsIgnoreCase(languageCode) && spinnerModel.getLanguageName().equalsIgnoreCase(languageName) && spinnerModel.getVersionCode().equalsIgnoreCase(versionCode)) {
//                // do nothing, same element selected again
//            } else {
//                // save to shared prefs
//                languageCode = spinnerModel.getLanguageCode();
//                languageName = spinnerModel.getLanguageName();
//                versionCode = spinnerModel.getVersionCode();
//                SharedPrefs.putString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, languageCode);
//                SharedPrefs.putString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_NAME, languageName);
//                SharedPrefs.putString(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, versionCode);
//                new AutographaRepository<LanguageModel>().addToNewContainer(languageCode, versionCode);
//                getAllBooks();
//                mLayoutManager.scrollToPositionWithOffset(0, 0);
//            }
//        }
//    }

//    @Override
//    public void onNothingSelected(AdapterView<?> parent) {
//    }

    private void getAllBooks() {
        mBookModelArrayList.clear();
        for (BookIdModel bookModel : Constants.CONTAINER_BOOKS_LIST) {
            mBookModelArrayList.add(bookModel);
        }
        mAdapter.notifyDataSetChanged();

        oldSection.setVisibility(View.GONE);
        newSection.setVisibility(View.GONE);
        boolean oldVis = false, newVis = false;
        for (int i=0; i<mBookModelArrayList.size(); i++) {
            if (mBookModelArrayList.get(i).getBookNumber() < 40) {
                oldVis = true;
                oldSection.setVisibility(View.VISIBLE);
            }
            if (mBookModelArrayList.get(i).getBookNumber() > 40) {
                newVis = true;
                newSection.setVisibility(View.VISIBLE);
            }
        }
        if ((oldVis || newVis) && !(oldVis && newVis)) {
            if (oldVis) {
                oldSection.setChecked(true);
            }
            if (newVis) {
                newSection.setChecked(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_title: {
                Intent intent = new Intent(this, AboutPageActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.iv_continue_reading: {
                String bookId = null, verse = null;
                int chapter = 1;
                String value = SharedPrefs.getString(Constants.PrefKeys.LAST_READ + "_" + languageCode + "_" + versionCode, null);
                if (value == null) {
                    break;
                }
                try {
                    JSONObject object = new JSONObject(value);
                    bookId = object.getString(Constants.PrefKeys.LAST_READ_BOOK_ID);
                    chapter = object.getInt(Constants.PrefKeys.LAST_READ_CHAPTER);
                    verse = object.getString(Constants.PrefKeys.LAST_READ_VERSE);
                } catch (JSONException je) {
                }
                if (bookId == null) {
                    break;
                }
                if (verse == null) {
                    break;
                }
                Intent readIntent = new Intent(this, BookActivity.class);
                readIntent.putExtra(Constants.Keys.BOOK_ID, bookId);
                readIntent.putExtra(Constants.Keys.CHAPTER_NO, chapter);
                readIntent.putExtra(Constants.Keys.VERSE_NO, verse);
                startActivity(readIntent);
                break;
            }
            case R.id.iv_notes: {
                Intent notesIntent = new Intent(this, NotesActivity.class);
                startActivity(notesIntent);
                break;
            }
            case R.id.iv_bookmark: {
                Intent menuIntent = new Intent(this, BookmarkActivity.class);
                startActivity(menuIntent);
                break;
            }
            case R.id.iv_highlights: {
                Intent menuIntent = new Intent(this, HighlightActivity.class);
                startActivity(menuIntent);
                break;
            }
            case R.id.iv_search: {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                break;
            }
            case R.id.iv_history: {
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            }
            case R.id.iv_settings: {
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingIntent, Constants.RequestCodes.SETTINGS);
                break;
            }
            case R.id.tv_language_version: {
                Intent intent = new Intent(this, SelectLanguageAndVersionActivity.class);
                intent.putExtra(Constants.Keys.SELECT_BOOK, true);
                intent.putExtra(Constants.Keys.LANGUAGE_CODE, languageCode);
                intent.putExtra(Constants.Keys.VERSION_CODE, versionCode);
                startActivityForResult(intent, Constants.RequestCodes.CHANGE_LANGUAGE_VERSION);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.RequestCodes.SETTINGS: {
                if (resultCode == RESULT_OK) {
                    if (mReadingMode != SharedPrefs.getReadingMode()) {
                        this.recreate();
                    } else if (mFontSize != SharedPrefs.getFontSize()) {
                        this.recreate();
                    }
//                    if (data.getBooleanExtra(Constants.Keys.RECREATE_NEEDED, false)) {
//                        this.recreate();
//                    }
                }
                break;
            }
            case Constants.RequestCodes.CHANGE_LANGUAGE_VERSION: {
                if (resultCode == RESULT_OK) {
                    languageCode = data.getStringExtra(Constants.Keys.LANGUAGE_CODE);
                    versionCode = data.getStringExtra(Constants.Keys.VERSION_CODE);
                    languageName = data.getStringExtra(Constants.Keys.LANGUAGE_NAME);

                    mTvLanguageVersion.setText(languageName + " " + versionCode);

                    SharedPrefs.putStringInstant(Constants.PrefKeys.LAST_OPEN_LANGUAGE_NAME, languageName);
                    SharedPrefs.putStringInstant(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, versionCode);
                    SharedPrefs.putStringInstant(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, languageCode);

                    new AutographaRepository<LanguageModel>().addToNewContainer(languageCode, versionCode);

                    getAllBooks();
                    mLayoutManager.scrollToPositionWithOffset(0, 0);
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onParsingComplete);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, Constants.VersionCodes.ULB).equals(versionCode) ||
                !SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, "ENG").equals(languageCode)) {
            versionCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, Constants.VersionCodes.ULB);
            languageCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, "ENG");
            languageName = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_NAME, "English");

            mTvLanguageVersion.setText(languageName + " " + versionCode);

            new AutographaRepository<LanguageModel>().addToNewContainer(languageCode, versionCode);

            getAllBooks();
            mLayoutManager.scrollToPositionWithOffset(0, 0);

//            SpinnerModel compareModel = new SpinnerModel();
//            compareModel.setLanguageName(languageName);
//            compareModel.setVersionCode(versionCode);
//            compareModel.setLanguageCode(languageCode);
//
//            int spinnerPosition = findIndex(compareModel);
//            mSpinner.setSelection(spinnerPosition);
        }
        if (Constants.CONTAINER_BOOKS_LIST.size() == 0) {
            // memory might be cleared, load all data again
            languageCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_CODE, "ENG");
            languageName = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_LANGUAGE_NAME, "English");
            versionCode = SharedPrefs.getString(Constants.PrefKeys.LAST_OPEN_VERSION_CODE, Constants.VersionCodes.ULB);
            new AutographaRepository<LanguageModel>().addToNewContainer(languageCode, versionCode);
            getAllBooks();
        }
    }

    private BroadcastReceiver onParsingComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            getCategories();
            String languageNameParsed = intent.getStringExtra(Constants.Keys.LANGUAGE_NAME);
            String versionCodeParsed = intent.getStringExtra(Constants.Keys.VERSION_CODE);
            Toast.makeText(HomeActivity.this, languageNameParsed + " " + versionCodeParsed + " " + context.getResources().getString(R.string.bible_download_finish), Toast.LENGTH_LONG).show();
            if (intent.getBooleanExtra(Constants.Keys.REFRESH_CONTAINER, false)) {
                new AutographaRepository<LanguageModel>().addToNewContainer(languageCode, versionCode);
                getAllBooks();
            }
        }
    };

}