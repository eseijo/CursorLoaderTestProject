package com.example.edgar.cursorloadertest;

/**
 * Created by edgar on 11/8/13.
 */

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class CursorLoaderListFragment extends ListFragment
        implements OnQueryTextListener, OnCloseListener,
        LoaderManager.LoaderCallbacks<Cursor> {


    ArrayList<String> res = new ArrayList<String>();

    String photoUri;

    ImageView iv;

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // The SearchView for doing filtering.
    SearchView mSearchView;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No phone numbers");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
Log.d("LOG123", "preadapter");
        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null,
                new String[] { Contacts.DISPLAY_NAME},
                new int[] { android.R.id.text1}, 0);
Log.d("LOG123", "presetadapter");
        setListAdapter(mAdapter);
Log.d("LOG123", "postsetadapter");

        // Start out with a progress indicator.
        setListShown(false);
Log.d("LOG123", "setlistshown");
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
Log.d("LOG123", "getloader");
    }

    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            Log.d("LOG123", "onactionviewinit");
            setQuery("", false);
            super.onActionViewCollapsed();
            Log.d("LOG123", "onactionviewcolapsed");
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new MySearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        item.setActionView(mSearchView);
        Log.d("LOG123", "oncreateoptions");
    }

    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        Log.d("LOG123", "onquerytextinit");
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) {
            Log.d("LOG123", "onquerytext");
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            Log.d("LOG123", "onquerytext");
            return true;
        }
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        Log.d("LOG123", "onquerytext");
        return true;
    }

    @Override public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    @Override
    public boolean onClose() {
        Log.d("LOG123", "oncloseinit");
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        Log.d("LOG123", "onclose");
        return true;
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.d("LOG123", "onListItem");
        Log.i("LOG123", "Item clicked: " + id);
        TextView nametv = (TextView) v;

        String name = nametv.getText().toString();

        Log.d("LOG123", name);

        Uri phoneUri;
        phoneUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                Uri.encode(name));

        Uri emailUri;
        emailUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
                Uri.encode(name));

        String[] PHONE_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        String[] EMAIL_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Email._ID,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
        };

        ContentResolver cr = getActivity().getContentResolver();

        Cursor curp = cr.query(phoneUri, PHONE_PROJECTION,
                null, null, null);

        Cursor cure = cr.query(emailUri, EMAIL_PROJECTION,
                null, null, null);

        res.add(name);
        curp.moveToNext();
        InputStream photoIS = openDisplayPhoto(Long.valueOf(curp.getString(0)));

        if(photoIS!=null){

            OutputStream out;
            String stateExternalStorage = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(stateExternalStorage)) {
                // availability
                try {
                    // Create path pointing to the root of the external storage
                    // (where application have permissions to save files)
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            name.replaceAll(" ","")+".jpg");
                    out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = photoIS.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    photoIS.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    Log.d("logMEDIA", e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("logMEDIA", e.getMessage());
                    e.printStackTrace();
                }
            }
        }



        res.add(curp.getString(2));
        while(curp.moveToNext()){
            res.add(curp.getString(2));
        }

        while(cure.moveToNext()){
            Log.d("LOG123", cure.getString(0)+"123123");
            res.add(cure.getString(1));
        }
        for(int i = 0; i<res.size(); i++){
            Log.d("LOG123", res.get(i).toString());
        }

    }

    public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getActivity().getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            Contacts._ID,
            Contacts.DISPLAY_NAME,
            Contacts.CONTACT_STATUS,
            Contacts.CONTACT_PRESENCE,
            Contacts.PHOTO_ID,
            Contacts.LOOKUP_KEY,
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                    Uri.encode(mCurFilter));
        } else {
            baseUri = Contacts.CONTENT_URI;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";
        Log.d("LOG123", "oncreateloader");
        return new CursorLoader(getActivity(), baseUri,
                CONTACTS_SUMMARY_PROJECTION, select, null,
                Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d("LOG123", "onloadfinishedinit");
        mAdapter.swapCursor(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
        Log.d("LOG123", "onloadfinished");
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        Log.d("LOG123", "onloaderresetinit");
        mAdapter.swapCursor(null);
        Log.d("LOG123", "onloaderreset");
    }

    @Override
    public void onResume() {
        super.onResume();
        iv = (ImageView) getView().findViewById(R.id.imageView);
    }
}