package com.happiness.faceplusplus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.faceplusplus.api.FaceDetecter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yalantis.ucrop.UCrop;
import com.zhy.http.okhttp.OkHttpUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity {
  protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
  protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

  private static final int REQUEST_SELECT_PICTURE = 0x01;
  private AlertDialog mAlertDialog;

  ImageView imageView;

  FaceDetecter faceDetecter;

  Uri selectedUri;

  FaceDetecter.Face[] face;

  private Target target = new Target() {
    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

      imageView.setImageBitmap(bitmap);

      face = faceDetecter.findFaces(bitmap);
    }

    @Override public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button btnPick = (Button) this.findViewById(R.id.pick);
    Button btnFace = (Button) this.findViewById(R.id.face);

    imageView = (ImageView) this.findViewById(R.id.image);

    btnPick.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        pickFromGallery();
      }
    });

    faceDetecter = new FaceDetecter();
    if (!faceDetecter.init(this, "efa13e5ac8e9cefca5a7a1d81f25b68a")) {
      Log.e("diff", "有错误 ");
    }
    faceDetecter.setTrackingMode(false);

    btnFace.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        Observable.create(new Observable.OnSubscribe<String>() {
          @Override public void call(Subscriber<? super String> subscriber) {

            if (face != null && face.length > 0){

              //OkHttpUtils.post().addFile()

            }
          }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
              @Override public void onCompleted() {

              }

              @Override public void onError(Throwable e) {

              }

              @Override public void onNext(String s) {

              }
            });
      }
    });
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          pickFromGallery();
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  /**
   * Requests given permission.
   * If the permission has been denied previously, a Dialog will prompt the user to grant the
   * permission, otherwise it is requested directly.
   */
  protected void requestPermission(final String permission, String rationale,
      final int requestCode) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
      showAlertDialog(getString(R.string.permission_title_rationale), rationale,
          new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission },
                  requestCode);
            }
          }, getString(R.string.label_ok), null, getString(R.string.label_cancel));
    } else {
      ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
    }
  }

  /**
   * This method shows dialog with given title & message.
   * Also there is an option to pass onClickListener for positive & negative button.
   *
   * @param title - dialog title
   * @param message - dialog message
   * @param onPositiveButtonClickListener - listener for positive button
   * @param positiveText - positive button text
   * @param onNegativeButtonClickListener - listener for negative button
   * @param negativeText - negative button text
   */
  protected void showAlertDialog(@Nullable String title, @Nullable String message,
      @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
      @NonNull String positiveText,
      @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener,
      @NonNull String negativeText) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    builder.setMessage(message);
    builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
    builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
    mAlertDialog = builder.show();
  }

  private void pickFromGallery() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
          getString(R.string.permission_read_storage_rationale),
          REQUEST_STORAGE_READ_ACCESS_PERMISSION);
    } else {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)),
          REQUEST_SELECT_PICTURE);
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == REQUEST_SELECT_PICTURE) {
        selectedUri = data.getData();
        if (selectedUri != null) {
          Picasso.with(this).load(selectedUri).into(target);
        } else {
          //Toast.makeText(SampleActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
        }
      } else if (requestCode == UCrop.REQUEST_CROP) {
        // handleCropResult(data);
      }
    }
    if (resultCode == UCrop.RESULT_ERROR) {
      //handleCropError(data);
    }
  }
}
