package com.sketchproject.infogue.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.utils.Constant;

import fr.ganfra.materialspinner.MaterialSpinner;
import jp.wasabeef.richeditor.RichEditor;
import me.gujun.android.taggroup.TagGroup;

public class ArticleCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String[] ITEMS = {"News", "Economic", "Entertainment", "Sport", "Science", "Technology", "Education", "Photo", "Video", "Others"};
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MaterialSpinner spinnerCategory = (MaterialSpinner) findViewById(R.id.spinner_category);
        spinnerCategory.setAdapter(adapterCategory);

        String[] SUBITEMS = {"SubItem 1", "SubItem 2", "SubItem 3", "SubItem 4", "SubItem 5", "SubItem 6"};
        ArrayAdapter<String> adapterSubItem = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SUBITEMS);
        adapterSubItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MaterialSpinner spinnerSubcategory = (MaterialSpinner) findViewById(R.id.spinner_subcategory);
        spinnerSubcategory.setAdapter(adapterSubItem);

        TagGroup mTagGroup = (TagGroup) findViewById(R.id.tag_group);
        //mTagGroup.setTags("Tag1", "Tag2", "Tag3");

        EditText mTitleView = (EditText) findViewById(R.id.input_title);
        if (mTitleView.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromInputMethod(mTitleView.getWindowToken(), 0);
        }

        final HorizontalScrollView control = (HorizontalScrollView) findViewById(R.id.editor_control);

        final RichEditor mEditor = (RichEditor) findViewById(R.id.input_content);
        mEditor.setEditorHeight(200);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder("Write article here...");
        mEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(40, 0, 40, 5);

                final EditText link = new EditText(v.getContext());
                link.setHint("Image link");
                link.setLayoutParams(params);
                layout.addView(link);

                final EditText title = new EditText(v.getContext());
                title.setHint("Alternative title");
                title.setLayoutParams(params);
                layout.addView(title);

                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Insert Image");
                builder.setMessage("Put complete image link (include http://) and alternative title.");
                builder.setView(layout);
                builder.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEditor.insertImage(link.getText().toString(), title.getText().toString());
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

                Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (mButton != null) {
                    mButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (mButton2 != null) {
                    mButton2.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(40, 0, 40, 5);

                final EditText link = new EditText(v.getContext());
                link.setHint("Link URL");
                link.setText(Constant.URL_APP);
                link.setLayoutParams(params);
                layout.addView(link);

                final EditText title = new EditText(v.getContext());
                title.setHint("Link title");
                title.setLayoutParams(params);
                layout.addView(title);

                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Insert Link");
                builder.setMessage("Put complete link url and link title.");
                builder.setView(layout);
                builder.setPositiveButton("Insert Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEditor.insertLink(link.getText().toString(), title.getText().toString());
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

                Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (mButton != null) {
                    mButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (mButton2 != null) {
                    mButton2.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            discardConfirmation();
        }
        else if(id == R.id.action_save){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle("Save Article");
            builder.setMessage("Publish and waiting for editor confirmation?");
            builder.setPositiveButton("Publish", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        discardConfirmation();
    }

    private void discardConfirmation() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard Article");
        builder.setMessage("Do you want to discard this article?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button mButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (mButton != null) {
            mButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        Button mButton2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (mButton2 != null) {
            mButton2.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }
}
