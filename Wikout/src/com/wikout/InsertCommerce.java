package com.wikout;

import io.backbeam.BackbeamException;
import io.backbeam.BackbeamObject;
import io.backbeam.FileUpload;
import io.backbeam.Location;
import io.backbeam.ObjectCallback;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import model.FontUtils;
import utils.CustomOnItemSelectedListener;
import utils.Photo;
import utils.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InsertCommerce extends ActionBarActivity {

	ImageView ivPhoto;
	EditText etPlacename;
	Button btnOk;
	Spinner spnCategory;
	TextView tvLocation;
	// Otras variables

	// Variables para controlar la fecha

	// variables para control de fotografias
	String position, photoName, url, photoPath, idoferta, idphoto = "";
	BackbeamObject objectphoto;
	File photo;
	double latitude, longitude;
	public int existPhoto = 0;

	// constantes utilizadas para lanzar intents
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int LOAD_IMAGE = 3;

	// Location de prueba
	public Location location = null;
	public String strLocation = null, address = null, city = null,
			country = null, straddress = null;

	// Constante para el picker

	final Context context = this;

	Util util = new Util();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.insert_commerce);
		util.projectData(context);
		FontUtils.setRobotoFont(context, ((Activity) context).getWindow()
				.getDecorView());
		initUI();

	}

	private void initUI() {
		getSupportActionBar().setTitle("Nuevo Comercio");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ivPhoto = (ImageView) findViewById(R.id.ivInsertCommercePhoto);
		etPlacename = (EditText) findViewById(R.id.etInsertCommercePlacename);
		btnOk = (Button) findViewById(R.id.btnInsertCommerceOk);
		spnCategory = (Spinner) findViewById(R.id.spnInsertCommerceCategory);
		tvLocation = (TextView) findViewById(R.id.tvInsertCommerceAddress);

		addListenerOnSpinnerItemSelection();
		if (etPlacename.isFocused()) {
			btnOk.requestFocus();
		}

		Bundle bundle = getIntent().getExtras();
		latitude = bundle.getDouble("pointlat");
		longitude = bundle.getDouble("pointlon");

		Geocoder geocoder = new Geocoder(context);
		List<Address> addresses = null;

		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);

		} catch (IOException e) {
			e.printStackTrace();
		}
		latitude = addresses.get(0).getLatitude();
		longitude = addresses.get(0).getLongitude();
		address = addresses.get(0).getAddressLine(0);
		city = addresses.get(0).getAddressLine(1);
		country = addresses.get(0).getAddressLine(2);
		straddress = address + " \n" + city + " " + country;
		strLocation = addresses.get(0).toString();
		System.out.println(strLocation);
		System.out.println(address);

		util.log("pancratio: " + address + city + country);
		tvLocation.setText(address + "\n" + city + ", " + country);

		// util.log("david: "+latitude+","+longitude);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (util.isNetworkAvailable(context) == true) {
					btnOk.setEnabled(false);
					ivPhoto.setEnabled(false);
					if (etPlacename.getText().length() == 0) {

						dialogIncompleteFields();
					} else {
						Bundle bundle = getIntent().getExtras();
						latitude = bundle.getDouble("pointlat");
						longitude = bundle.getDouble("pointlon");
						if (photo != null) {
							setSupportProgressBarIndeterminateVisibility(true);
							insertComercePhoto(util.actualDate());
						} else {
							setSupportProgressBarIndeterminateVisibility(true);
							util.log("no hay foto");
							insertNewCommerce(objectphoto);
						}

						// imageClicked(v);
					}
				} else {
					util.showInfoDialog(context, "Lo sentimos",
							"Es necesaria conexi�n a internet");
				}
			}
		});
		setSupportProgressBarIndeterminateVisibility(false);
	}

	public void dialogIncompleteFields() {
		AlertDialog.Builder dialogIncomplete = new AlertDialog.Builder(this);
		dialogIncomplete.setTitle("Informaci�n incompleta");
		dialogIncomplete
				.setMessage("Rellena los campos Incompletos, por favor.");
		dialogIncomplete.setCancelable(false);
		dialogIncomplete.setNeutralButton("Aceptar",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogo1, int id) {

					}

				});

		dialogIncomplete.show();
	}

	public void addListenerOnSpinnerItemSelection() {

		spnCategory
				.setOnItemSelectedListener(new CustomOnItemSelectedListener());

	}

	// METODO PARA SUBIR FOTO de comercio
	protected void insertComercePhoto(final Date createdate) {
		final BackbeamObject objectPhoto = new BackbeamObject("file");
		// Hay que pasarle el objeto de tipo file "foto"
		objectPhoto.uploadFile(new FileUpload(photo, "image/jpg"),
				new ObjectCallback() {
					@Override
					public void success(BackbeamObject photo) {
						System.out.println("success!! " + photo.getId());
						photo.setString("idphoto", photo.getId());
						photo.setDate("uploaddate", createdate);
						photo.save(new ObjectCallback() {
							@Override
							public void success(BackbeamObject objetofoto) {
								System.out.println("foto subida con �xito!! "
										+ objetofoto.getId());
								insertNewCommerce(objetofoto);
							}
						});
					}

					@Override
					public void failure(BackbeamException exception) {
						System.out.println("failure!");
						exception.printStackTrace();
					}
				});
	}

	// INSERTAR NUEVO "NEW COMMERCE"
	protected void insertNewCommerce(BackbeamObject objectphoto) {

		location = new Location(latitude, longitude, strLocation);

		// Extraigo la fecha actual
		Calendar calendar = new GregorianCalendar();
		final Date createdate = calendar.getTime();
		// Creo el objeto commerce
		final BackbeamObject commerce = new BackbeamObject("commerce");

		if (objectphoto != null) {
			commerce.setObject("file", objectphoto);
		}

		// Relleno los campos del objeto
		commerce.setString("placename", etPlacename.getText().toString());
		util.log("david2: " + latitude + "," + longitude);

		commerce.setLocation("placelocation", location);
		commerce.setString("category", spnCategory.getSelectedItem().toString());
		commerce.setDate("commercecreationdate", createdate);
		commerce.setString("udid", getId());
		commerce.setNumber("actualoffers", 0);

		commerce.setNumber("numbubble", 0);
		// Guardo el objeto
		commerce.save(new ObjectCallback() {
			@Override
			public void success(BackbeamObject commerce) {
				// Llamo al metodo insertPhoto para enlazarlo con la foto

				util.log("subido");
				Intent insertoffer = new Intent();
				insertoffer.putExtra("idcommerce", commerce.getId());
				insertoffer.putExtra("placename",
						commerce.getString("placename"));
				double comlat = commerce.getLocation("placelocation")
						.getLatitude();
				double comlon = commerce.getLocation("placelocation")
						.getLongitude();
				insertoffer.putExtra("commercelat", comlat);
				System.out.println("commercelat insert: " + comlat);
				insertoffer.putExtra("commercelon", comlon);
				insertoffer.putExtra("location",
						commerce.getLocation("placelocation").getAddress()
								.toString());
				insertoffer.putExtra("address", straddress);
				setResult(RESULT_OK, insertoffer);
				util.showToast(context, "comercio creado");
				setSupportProgressBarIndeterminateVisibility(false);
				finish();

			}
		});
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_BACK:
			System.out.println("entra aqui");
			CommerceList.actionbarAct.finish();
			finish();

			return true;
		}
		return false;
	}

	private String getId() {
		String id = android.provider.Settings.System.getString(
				super.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		return id;
	}

	public void imageClicked(View imageView) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.question)
				.setPositiveButton(R.string.camera,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onPhotoDialogPositiveClick();
							}
						})
				.setNegativeButton(R.string.galeria,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onPhotoDialogNegativeClick();
							}
						});

		// launch dialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

	}

	// launching camera activity
	public void onPhotoDialogPositiveClick() {
		// launch camera
		Intent picture = new Intent(this, Photo.class);
		picture.putExtra("ACTION_REQUESTED", "CAMERA");
		startActivityForResult(picture, REQUEST_IMAGE_CAPTURE);
	}

	// if gallery has been pushed
	public void onPhotoDialogNegativeClick() {
		// launch gallery
		Intent picture = new Intent(this, Photo.class);
		picture.putExtra("ACTION_REQUESTED", "GALLERY");
		startActivityForResult(picture, LOAD_IMAGE);
	}

	// load image in imageView
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// if there's no errors, the image is loaded
		if (resultCode == 10000) {

			Bundle photoBundle = new Bundle();
			util.log("bundle created");
			photoBundle = data.getExtras();

			photoPath = photoBundle.getString("path");
			photoName = photoBundle.getString("photoName");
			url = photoBundle.getString("url");
			photo = (File) photoBundle.get("photo");

			util.log(photoBundle.getString("photoName"));

			Bitmap cphoto = BitmapFactory.decodeFile(photoPath);
			ivPhoto.setImageBitmap(cphoto);
			existPhoto = 1;

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
