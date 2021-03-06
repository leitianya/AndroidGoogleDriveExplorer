/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appmogli.gdriveexplorer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;

/**
 * Asynchronously load the tasks with a progress dialog.
 * 
 * @author Yaniv Inbar
 */
class AsyncLoadFiles extends AsyncTask<Void, Void, List<String>> {

  private final DriveSample driveSample;
  private final ProgressDialog dialog;
  private Drive service;
  private File rootFile;

  AsyncLoadFiles(DriveSample driveSample) {
    this.driveSample = driveSample;
    service = driveSample.service;
    dialog = new ProgressDialog(driveSample);
  }

  @Override
  protected void onPreExecute() {
    dialog.setMessage("Loading root...");
    dialog.show();
  }

  @Override
  protected List<String> doInBackground(Void... arg0) {
    try {
        About about = service.about().get().execute();

      List<String> result = new ArrayList<String>();
//       com.google.api.services.drive.Drive.Children.List fileListRequest = service.children().list(about.getRootFolderId());
//      List<ChildReference> children = fileListRequest.execute().getItems();
//      if (children != null) {
//        for (ChildReference child: children) {
//          Get fileRequest = service.files().get(child.getId());	
//          result.add(fileRequest.execute().toPrettyString());
//        }
//      } else {
//        result.add("No tasks.");
//      }
      rootFile = service.files().get(about.getRootFolderId()).execute();
      result.add(rootFile.getTitle());
      return result;
    } catch (IOException e) {
      driveSample.handleGoogleException(e);
      return Collections.singletonList(e.getMessage());
    } finally {
      driveSample.onRequestCompleted();
    }
  }

  @Override
  protected void onPostExecute(List<String> result) {
    dialog.dismiss();
    driveSample.setListAdapter(
        new ArrayAdapter<String>(driveSample, android.R.layout.simple_list_item_1, result));
    driveSample.getListView().setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent(driveSample, DocListActivity.class);
			intent.putExtra(DocListActivity.KEY_ROOT_FOLDER_ID, rootFile.getId());
			intent.putExtra(DocListActivity.KEY_AUTH_TOKEN, driveSample.credential.getAccessToken());
			driveSample.startActivity(intent);
		}
	});
  }
}
