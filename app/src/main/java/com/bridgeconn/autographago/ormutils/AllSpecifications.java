package com.bridgeconn.autographago.ormutils;

import com.bridgeconn.autographago.models.BookModel;
import com.bridgeconn.autographago.models.ChapterModel;
import com.bridgeconn.autographago.models.LanguageModel;
import com.bridgeconn.autographago.models.NotesModel;
import com.bridgeconn.autographago.models.SearchHistoryModel;
import com.bridgeconn.autographago.models.VerseComponentsModel;
import com.bridgeconn.autographago.models.VersionModel;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class AllSpecifications {

    public static class AllLanguages implements Specification<LanguageModel> {
        @Override
        public RealmResults<LanguageModel> generateResults(Realm realm) {
            return realm.where(LanguageModel.class).findAllSorted("languageCode");
        }
    }

    public static class AllVersions implements Specification<VersionModel> {
        @Override
        public RealmResults<VersionModel> generateResults(Realm realm) {
            return realm.where(VersionModel.class).findAll();
        }
    }

    public static class AllBooks implements Specification<BookModel> {
        @Override
        public RealmResults<BookModel> generateResults(Realm realm) {
            return realm.where(BookModel.class).findAll();
        }
    }

    public static class AllChapters implements Specification<ChapterModel> {
        @Override
        public RealmResults<ChapterModel> generateResults(Realm realm) {
            return realm.where(ChapterModel.class).findAll();
        }
    }

    public static class AllVerseComponents implements Specification<VerseComponentsModel> {
        @Override
        public RealmResults<VerseComponentsModel> generateResults(Realm realm) {
            return realm.where(VerseComponentsModel.class).findAll();
        }
    }

    public static class BookModelById implements Specification<BookModel> {
        private String bookId;

        public BookModelById(String bookId) {
            this.bookId = bookId;
        }

        @Override
        public RealmResults<BookModel> generateResults(Realm realm) {
            RealmQuery<BookModel> query = realm.where(BookModel.class);
            query = query.equalTo("bookId", bookId);
            return query.findAll();
        }
    }

    public static class SearchInBookName implements Specification<BookModel> {
        private String text;

        public SearchInBookName(String text) {
            this.text = text;
        }

        @Override
        public RealmResults<BookModel> generateResults(Realm realm) {
            RealmQuery<BookModel> query = realm.where(BookModel.class);
            query = query.contains("bookName", text, Case.INSENSITIVE);
            return query.findAll();
        }
    }

    public static class SearchInVerseComponentsText implements Specification<VerseComponentsModel> {
        private String text;

        public SearchInVerseComponentsText(String text) {
            this.text = text;
        }

        @Override
        public RealmResults<VerseComponentsModel> generateResults(Realm realm) {
            RealmQuery<VerseComponentsModel> query = realm.where(VerseComponentsModel.class);
            query = query.contains("text", text, Case.INSENSITIVE);
            return query.findAll().distinct("verseNumber");
        }
    }

    public static class AllSearchHistories implements Specification<SearchHistoryModel> {
        @Override
        public RealmResults<SearchHistoryModel> generateResults(Realm realm) {
            return realm.where(SearchHistoryModel.class).findAll();
        }
    }

    public static class SearchHistoryModelByText implements Specification<SearchHistoryModel> {
        private String text;

        public SearchHistoryModelByText(String text) {
            this.text = text;
        }

        @Override
        public RealmResults<SearchHistoryModel> generateResults(Realm realm) {
            RealmQuery<SearchHistoryModel> query = realm.where(SearchHistoryModel.class);
            query = query.equalTo("searchText", text, Case.INSENSITIVE);
            return query.findAllSorted("searchCount", Sort.DESCENDING);
        }
    }

    public static class AllBookmarks implements Specification<BookModel> {
        @Override
        public RealmResults<BookModel> generateResults(Realm realm) {
            RealmQuery<BookModel> query = realm.where(BookModel.class);
            query = query.greaterThan("bookmarkChapterNumber", 0);
            return query.findAll();
        }
    }

    public static class AllNotes implements Specification<NotesModel> {
        @Override
        public RealmResults<NotesModel> generateResults(Realm realm) {
            return realm.where(NotesModel.class).findAll();
        }
    }

    public static class NotesById implements Specification<NotesModel> {
        private long id;

        public NotesById(long id) {
            this.id = id;
        }

        @Override
        public RealmResults<NotesModel> generateResults(Realm realm) {
            RealmQuery<NotesModel> query = realm.where(NotesModel.class);
            query = query.equalTo("timestamp", id);
            return query.findAll();
        }
    }

}