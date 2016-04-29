package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.AlertFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Contributor;
import com.sketchproject.infogue.models.Repositories.CategoryRepository;
import com.sketchproject.infogue.models.Repositories.SubcategoryRepository;
import com.sketchproject.infogue.models.Subcategory;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.RealPathResolver;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.Validator;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import jp.wasabeef.richeditor.RichEditor;
import me.gujun.android.taggroup.TagGroup;

public class ArticleCreateActivity extends AppCompatActivity implements Validator.ViewValidation {

    public static final String CALLED_FROM_MAIN = "fromMainActivity";
    public static final String RESULT_CODE = "resultCode";
    public static final int CALL_ARTICLE_FORM_CODE = 100;

    protected final int PICK_IMAGE_REQUEST = 1;

    protected Validator validator;
    protected SessionManager session;
    protected ConnectionDetector connectionDetector;
    protected AlertFragment alert;
    protected Article article;
    protected List<String> validationMessage;
    protected ProgressDialog progress;
    protected ScrollView mScrollView;

    protected AlertDialog dialogDiscard;
    protected EditText mTitleInput;
    protected MaterialSpinner mCategorySpinner;
    protected MaterialSpinner mSubcategorySpinner;
    protected ImageView mFeaturedImage;
    protected Button mSelectButton;
    protected TagGroup mTagsInput;
    protected EditText mSlugInput;
    protected RichEditor mContentEditor;
    protected EditText mExcerptInput;
    protected RadioButton mPublishedRadio;
    protected RadioButton mDraftRadio;
    protected Button mSaveButton;

    protected String[] mCategoryList = {};
    protected String[] mSubcategoryList = {};
    protected ArrayAdapter<String> adapterCategory;
    protected ArrayAdapter adapterSubcategory;
    protected List<Category> categoriesList;
    protected List<Subcategory> subcategoriesList;

    protected String realPathFeatured;
    protected boolean isCalledFromMainActivity;
    protected boolean isNewFeatured;
    protected boolean isUpdate;
    protected String apiUrl;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        isCalledFromMainActivity = extras != null && extras.getBoolean(CALLED_FROM_MAIN, false);

        validator = new Validator();
        session = new SessionManager(getBaseContext());
        connectionDetector = new ConnectionDetector(getBaseContext());

        mScrollView = (ScrollView) findViewById(R.id.scroll_container);
        mTitleInput = (EditText) findViewById(R.id.input_title);
        mCategorySpinner = (MaterialSpinner) findViewById(R.id.spinner_category);
        mSubcategorySpinner = (MaterialSpinner) findViewById(R.id.spinner_subcategory);
        mFeaturedImage = (ImageView) findViewById(R.id.featured_image);
        mSelectButton = (Button) findViewById(R.id.btn_select_featured);
        mTagsInput = (TagGroup) findViewById(R.id.input_tags);
        mSlugInput = (EditText) findViewById(R.id.input_slug);
        mContentEditor = (RichEditor) findViewById(R.id.input_content);
        mExcerptInput = (EditText) findViewById(R.id.input_excerpt);
        mPublishedRadio = (RadioButton) findViewById(R.id.radio_published);
        mDraftRadio = (RadioButton) findViewById(R.id.radio_draft);
        mSaveButton = (Button) findViewById(R.id.btn_save_article);

        if (mTitleInput.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromInputMethod(mTitleInput.getWindowToken(), 0);
        }
        mTitleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSlugInput.setText(createSlug(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // populate category spinner
        categoriesList = new CategoryRepository().retrieveData();
        mCategoryList = new String[categoriesList.size()];
        for (int k = 0; k < categoriesList.size(); k++) {
            mCategoryList[k] = categoriesList.get(k).getCategory();
        }
        adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCategoryList);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapterCategory);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Infogue/Category", "position " + position + " id " + id);
                if (position >= 0) {
                    populateSubcategory(position);
                } else {
                    mSubcategoryList = new String[0];
                    // populate subcategory spinner
                    adapterSubcategory = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, mSubcategoryList);
                    adapterSubcategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSubcategorySpinner.setAdapter(adapterSubcategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // populate subcategory spinner
        adapterSubcategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSubcategoryList);
        adapterSubcategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubcategorySpinner.setAdapter(adapterSubcategory);
        mSubcategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Infogue/SubCategory", "position " + position + " id " + id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTagsInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            Log.i("INFOGUE/Article", "Submit Tag");
                            mTagsInput.submitTag();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        mTagsInput.submitTag();

        final HorizontalScrollView control = (HorizontalScrollView) findViewById(R.id.editor_control);
        mContentEditor.setEditorHeight(200);
        mContentEditor.setPadding(10, 10, 10, 10);
        mContentEditor.setPlaceholder("Write article here...");
        mContentEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    HorizontalScrollView control = (HorizontalScrollView) findViewById(R.id.editor_control);
                    control.setVisibility(View.VISIBLE);
                } else {
                    control.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setItalic();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(40, 0, 40, 5);

                final EditText link = new EditText(v.getContext());
                link.setHint("Image link");
                link.setLayoutParams(params);
                layout.addView(link);

                final EditText title = new EditText(v.getContext());
                title.setHint("Alternative title");
                title.setLayoutParams(params);
                layout.addView(title);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ArticleCreateActivity.this);
                builder.setTitle("Insert Image");
                builder.setMessage("Put complete image link and alternative title.");
                builder.setView(layout);
                builder.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContentEditor.insertImage(link.getText().toString(), title.getText().toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                Helper.setDialogButtonTheme(v.getContext(), dialog);
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(40, 0, 40, 5);

                final EditText link = new EditText(v.getContext());
                link.setHint("Link URL");
                link.setText(APIBuilder.URL_APP);
                link.setLayoutParams(params);
                layout.addView(link);

                final EditText title = new EditText(v.getContext());
                title.setHint("Link title");
                title.setLayoutParams(params);
                layout.addView(title);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ArticleCreateActivity.this);
                builder.setTitle("Insert Link");
                builder.setMessage("Put complete link url and link title.");
                builder.setView(layout);
                builder.setPositiveButton("Insert Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContentEditor.insertLink(link.getText().toString(), title.getText().toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                Helper.setDialogButtonTheme(v.getContext(), dialog);
            }
        });

        mSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Featured"), PICK_IMAGE_REQUEST);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preValidation();
                postValidation(onValidateView());
            }
        });

        alert = (AlertFragment) getSupportFragmentManager().findFragmentById(R.id.alert_fragment);
        progress = new ProgressDialog(ArticleCreateActivity.this);
        progress.setIndeterminate(true);

        isNewFeatured = false;
        apiUrl = APIBuilder.URL_API_ARTICLE;
        isUpdate = false;
    }

    protected void populateSubcategory(int position) {
        subcategoriesList = new SubcategoryRepository().retrieveData(categoriesList.get(position).getId());
        mSubcategoryList = new String[subcategoriesList.size()];
        for (int k = 0; k < subcategoriesList.size(); k++) {
            mSubcategoryList[k] = subcategoriesList.get(k).getSubcategory();
        }

        // populate subcategory spinner
        adapterSubcategory = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, mSubcategoryList);
        adapterSubcategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubcategorySpinner.setAdapter(adapterSubcategory);
        if (mSubcategoryList.length > 0) {
            mSubcategorySpinner.setSelection(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            if (Build.VERSION.SDK_INT < 19) {
                realPathFeatured = RealPathResolver.getRealPathFromURI_API11to18(getBaseContext(), data.getData());
            } else {
                realPathFeatured = RealPathResolver.getRealPathFromURI_API19(getBaseContext(), data.getData());
            }

            isNewFeatured = true;

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mFeaturedImage.setImageBitmap(bitmap);
                mFeaturedImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            discardConfirmation();
        } else if (id == R.id.action_save) {
            preValidation();
            postValidation(onValidateView());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (dialogDiscard != null && dialogDiscard.isShowing()) {
            dialogDiscard.cancel();
        } else {
            discardConfirmation();
        }
    }

    protected void saveConfirmation() {
        Log.i("Infogue/Sub id", String.valueOf(article.getSubcategoryId()));

        int action;
        if (mPublishedRadio.isChecked()) {
            action = R.string.action_save_publish;
        } else if (mDraftRadio.isChecked()) {
            action = R.string.action_save_draft;
        } else {
            action = 0;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.label_dialog_save_article);
        builder.setMessage(R.string.message_save_article);
        builder.setPositiveButton(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveData();
            }
        });

        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialogSave = builder.create();
        dialogSave.show();
        Helper.setDialogButtonTheme(this, dialogSave);
    }

    protected void discardConfirmation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.label_dialog_discard_article));
        builder.setMessage(getString(R.string.message_discard_article));
        builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isCalledFromMainActivity) {
                    Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                    articleIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                    articleIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
                    // add some information for notification
                    articleIntent.putExtra(ArticleActivity.DISCARD_ARTICLE, true);
                    articleIntent.putExtra(CALLED_FROM_MAIN, isCalledFromMainActivity);
                    articleIntent.putExtra(RESULT_CODE, AppCompatActivity.RESULT_CANCELED);

                    startActivity(articleIntent);
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(ArticleActivity.DISCARD_ARTICLE, true);
                    setResult(AppCompatActivity.RESULT_CANCELED, returnIntent);
                }

                finish();
            }
        });

        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogDiscard = builder.create();
        dialogDiscard.show();
        Helper.setDialogButtonTheme(this, dialogDiscard);
    }

    @Override
    public void preValidation() {
        article = new Article();
        article.setTitle(mTitleInput.getText().toString());
        article.setSlug(mSlugInput.getText().toString());

        if (categoriesList != null) {
            int index = mCategorySpinner.getSelectedItemPosition() - 1;
            Log.i("Infogue/Category", "Selected index "+String.valueOf(index));

            if(mCategorySpinner.getSelectedItemPosition() - 1 >= 0){
                int categoryId = categoriesList.get(index).getId();
                String categoryLabel = categoriesList.get(index).getCategory();

                article.setCategoryId(categoryId);
                article.setCategory(categoryLabel);
            }
            else{
                article.setCategoryId(0);
                article.setCategory(null);
            }
            Log.i("Infogue/Category", "Id " + article.getCategoryId() + " Label " + article.getCategory());
        } else {
            Log.e("Infogue/Category", "is null");
        }

        if (subcategoriesList != null) {
            int index = mSubcategorySpinner.getSelectedItemPosition() - 1;
            Log.i("Infogue/Subcategory", String.valueOf(index));
            if(index >= 0){
                int subcategoryId = subcategoriesList.get(index).getId();
                String subcategoryLabel = subcategoriesList.get(index).getSubcategory();

                article.setSubcategoryId(subcategoryId);
                article.setSubcategory(subcategoryLabel);
            }
            else{
                article.setSubcategoryId(0);
                article.setSubcategory(null);
            }

            Log.i("Infogue/Subcategory", "Id " + article.getSubcategoryId() + " Label " + article.getSubcategory());
        } else {
            Log.e("Infogue/Subcategory", "is null");
        }

        article.setFeatured(realPathFeatured);
        article.setTags(new ArrayList<>(Arrays.asList(mTagsInput.getTags())));
        article.setSlug(mSlugInput.getText().toString().trim());
        article.setContent(mContentEditor.getHtml());
        article.setExcerpt(mExcerptInput.getText().toString());
        article.setStatus(mPublishedRadio.isChecked() ? Article.STATUS_PENDING.toLowerCase() : mDraftRadio.isChecked() ? Article.STATUS_DRAFT.toLowerCase() : "Invalid Selected");
        article.setAuthorId(session.getSessionData(SessionManager.KEY_ID, 0));
    }

    @Override
    public boolean onValidateView() {
        validationMessage = new ArrayList<>();
        boolean isInvalid = false;
        View focusView = null;

        // validation of title
        boolean isTitleEmpty = validator.isEmpty(article.getTitle(), true);
        boolean isTitleValidLength = validator.rangeLength(article.getTitle(), 5, 75);
        if (isTitleEmpty || !isTitleValidLength) {
            if (isTitleEmpty) {
                validationMessage.add(getString(R.string.error_title_required));
            } else {
                validationMessage.add(getString(R.string.error_title_range));
            }
            focusView = mTitleInput;
            isInvalid = true;
        }

        // validation of category
        boolean isCategoryIdEmpty = validator.isEmpty(article.getCategoryId(), true);
        boolean isCategoryEmpty = validator.isEmpty(article.getCategory(), true);
        if (isCategoryIdEmpty || isCategoryEmpty) {
            validationMessage.add(getString(R.string.error_category_required));
            focusView = mCategorySpinner;
            isInvalid = true;
        }

        // validation of subcategory
        boolean isSubCategoryIdEmpty = validator.isEmpty(article.getSubcategoryId(), true);
        boolean isSubCategoryEmpty = validator.isEmpty(article.getSubcategory(), true);
        if (isSubCategoryIdEmpty || isSubCategoryEmpty) {
            validationMessage.add(getString(R.string.error_subcategory_required));
            focusView = mSubcategorySpinner;
            isInvalid = true;
        }

        // validation of featured
        boolean isFeaturedEmpty = validator.isEmpty(article.getFeatured());
        if (isFeaturedEmpty) {
            validationMessage.add(getString(R.string.error_featured_required));
            focusView = mSelectButton;
            isInvalid = true;
        }

        // validation of tags
        boolean isTagsEmpty = validator.isEmpty(TextUtils.join(",", article.getTags()));
        if (isTagsEmpty) {
            validationMessage.add(getString(R.string.error_tags_required));
            focusView = mTagsInput;
            isInvalid = true;
        }

        // validation of slug
        boolean isSlugEmpty = validator.isEmpty(article.getSlug(), true);
        boolean isSlugValid = validator.isAlphaDash(article.getSlug());
        if (isSlugEmpty || !isSlugValid) {
            if (isSlugEmpty) {
                validationMessage.add(getString(R.string.error_slug_required));
            } else {
                validationMessage.add(getString(R.string.error_slug_invalid));
            }
            focusView = mSlugInput;
            isInvalid = true;
        }

        // validation of content
        boolean isContentEmpty = validator.isEmpty(article.getContent());
        if (isContentEmpty) {
            validationMessage.add(getString(R.string.error_content_required));
            focusView = mContentEditor;
            isInvalid = true;
        }

        // validation of excerpt
        boolean isExcerptLengthValid = validator.maxLength(article.getExcerpt(), 300);
        if (!isExcerptLengthValid) {
            validationMessage.add(getString(R.string.error_excerpt_length));
            focusView = mExcerptInput;
            isInvalid = true;
        }

        // validation of status
        boolean isStatusValid = validator.isMemberOf(article.getStatus(),
                new String[]{
                        Article.STATUS_PUBLISHED.toLowerCase(),
                        Article.STATUS_DRAFT.toLowerCase(),
                        Article.STATUS_PENDING.toLowerCase()
                });
        if (!isStatusValid) {
            validationMessage.add(getString(R.string.error_status_invalid));
            focusView = mExcerptInput;
            isInvalid = true;
        }

        // validation of author
        boolean isAuthorEmpty = validator.isEmpty(article.getAuthorId());
        if (isAuthorEmpty) {
            validationMessage.add(getString(R.string.error_session_required));
            isInvalid = true;
        }

        if (isInvalid && focusView != null) {
            focusView.requestFocus();
        }

        return !isInvalid;
    }

    @Override
    public void postValidation(boolean isValid) {
        if (isValid) {
            alert.dismiss();
            saveConfirmation();
        } else {
            mScrollView.smoothScrollTo(0, 0);
            alert.setAlertType(AlertFragment.ALERT_WARNING);
            alert.setAlertTitle(getString(R.string.message_validation_warning));
            alert.setAlertMessage(validationMessage);
            alert.show();
        }
    }

    protected void saveData() {
        if (connectionDetector.isNetworkAvailable()) {
            progress.setMessage(getString(R.string.label_save_article_progress));
            progress.show();

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiUrl, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String resultResponse = new String(response.data);
                    try {
                        JSONObject result = new JSONObject(resultResponse);
                        String status = result.getString(APIBuilder.RESPONSE_STATUS);
                        String message = result.getString(APIBuilder.RESPONSE_MESSAGE);
                        Log.i("Infogue/Article", message);

                        if (status.equals(APIBuilder.REQUEST_SUCCESS)) {
                            if (isCalledFromMainActivity) {
                                Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);
                                articleIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                                articleIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
                                // add some information for notification
                                articleIntent.putExtra(ArticleActivity.SAVE_ARTICLE, true);
                                articleIntent.putExtra(CALLED_FROM_MAIN, isCalledFromMainActivity);
                                articleIntent.putExtra(RESULT_CODE, AppCompatActivity.RESULT_OK);
                                startActivity(articleIntent);
                            } else {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(ArticleActivity.SAVE_ARTICLE, true);
                                returnIntent.putExtra(CALLED_FROM_MAIN, isCalledFromMainActivity);
                                setResult(AppCompatActivity.RESULT_OK, returnIntent);
                            }
                            finish();

                        } else {
                            alert.setAlertType(AlertFragment.ALERT_INFO);
                            alert.setAlertMessage(getString(R.string.error_unknown));
                            alert.show();
                            mScrollView.smoothScrollTo(0, 0);
                        }
                        realPathFeatured = null;

                    } catch (JSONException e) {
                        e.printStackTrace();
                        alert.setAlertType(AlertFragment.ALERT_WARNING);
                        alert.setAlertMessage(getString(R.string.error_parse_data));
                        alert.show();
                        mScrollView.smoothScrollTo(0, 0);
                    }
                    progress.cancel();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();

                    NetworkResponse networkResponse = error.networkResponse;
                    String errorMessage = getString(R.string.error_unknown);
                    if (networkResponse == null) {
                        if (error.getClass().equals(TimeoutError.class)) {
                            errorMessage = getString(R.string.error_timeout);
                        }
                    } else {
                        try {
                            String result = new String(networkResponse.data);
                            JSONObject response = new JSONObject(result);
                            String status = response.optString(APIBuilder.RESPONSE_STATUS);
                            String message = response.optString(APIBuilder.RESPONSE_MESSAGE);

                            Log.e("Infogue/Article", "Error::" + message);

                            if (status.equals(APIBuilder.REQUEST_DENIED) && networkResponse.statusCode == 400) {
                                errorMessage = message;
                            } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 401) {
                                errorMessage = getString(R.string.error_unauthorized);
                            } else if (status.equals(APIBuilder.REQUEST_NOT_FOUND) && networkResponse.statusCode == 404) {
                                errorMessage = getString(R.string.error_not_found);
                            } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 503) {
                                errorMessage = getString(R.string.error_maintenance);
                            } else if (status.equals(APIBuilder.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                errorMessage = message;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorMessage = getString(R.string.error_parse_data);
                        }
                    }

                    alert.setAlertType(AlertFragment.ALERT_DANGER);
                    alert.setAlertMessage(errorMessage);
                    alert.show();

                    mScrollView.smoothScrollTo(0, 0);
                    progress.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(Contributor.CONTRIBUTOR_API, session.getSessionData(SessionManager.KEY_TOKEN, null));
                    params.put(Contributor.CONTRIBUTOR_FOREIGN, String.valueOf(session.getSessionData(SessionManager.KEY_ID, 0)));
                    params.put(Article.ARTICLE_TITLE, article.getTitle());
                    params.put(Article.ARTICLE_SLUG, article.getSlug());
                    params.put(Article.ARTICLE_TAGS, TextUtils.join(",", article.getTags()));
                    params.put(Article.ARTICLE_SUBCATEGORY_ID, String.valueOf(article.getSubcategoryId()));
                    params.put(Article.ARTICLE_CONTENT, article.getContent());
                    params.put(Article.ARTICLE_EXCERPT, article.getExcerpt());
                    params.put(Article.ARTICLE_STATUS, article.getStatus());
                    if (isUpdate) {
                        params.put("_method", "put");
                    }
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    if (realPathFeatured != null && !realPathFeatured.isEmpty() && isNewFeatured) {
                        byte[] featuredData = Helper.getFileDataFromDrawable(mFeaturedImage.getDrawable());
                        params.put(Article.ARTICLE_FEATURED, new DataPart("file_featured.jpg", featuredData, "image/jpeg"));
                    }

                    return params;
                }
            };
            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
        } else {
            connectionDetector.snackbarDisconnectNotification(mSelectButton, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectionDetector.dismissNotification();
                }
            });
        }
    }

    protected String createSlug(String title) {
        String trimmed = title.trim();
        String slug = trimmed
                .replaceAll("[^a-zA-Z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.toLowerCase();
    }
}
