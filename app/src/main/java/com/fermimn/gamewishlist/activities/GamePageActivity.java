package com.fermimn.gamewishlist.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fermimn.gamewishlist.R;
import com.fermimn.gamewishlist.data_types.Game;
import com.fermimn.gamewishlist.data_types.GamePreview;
import com.fermimn.gamewishlist.data_types.Promo;
import com.fermimn.gamewishlist.repositories.WishListRepository;
import com.fermimn.gamewishlist.utils.Gamestop;
import com.fermimn.gamewishlist.utils.Util;
import com.fermimn.gamewishlist.utils.WishlistManager;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GamePageActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = GamePageActivity.class.getSimpleName();

    private Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);

        // get game ID
        String id = getIntent().getStringExtra("gameID");

        // search the game in the wishlist
        WishlistManager wishlistManager = WishlistManager.getInstance(this);
        for (GamePreview gamePreview : wishlistManager.getWishlist()){
            if (gamePreview.getId().equals(id)) {
                mGame = (Game) gamePreview;
                break;
            }
        }

        // if the is not in the wishlist
        if (mGame == null) {
            // download the game
            DownloadGame task = new DownloadGame(this);
            task.execute(id);
        } else {
            // set the UI
            updateUI(mGame);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_game_page, menu);

        MenuItem actionAdd = menu.findItem(R.id.action_add_game).setVisible(false);
        MenuItem actionRemove = menu.findItem(R.id.action_remove_game).setVisible(false);

        // show add button if the game is not in the wishlist
        // show remove button if the game is already in the wishlist
        if (mGame != null) {
            setTitle( mGame.getTitle() );
            WishlistManager wishlistManager = WishlistManager.getInstance(this);
            boolean result = wishlistManager.getWishlist().contains(mGame);
            if (result) {
                actionRemove.setVisible(true);
            } else {
                actionAdd.setVisible(true);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_add_game:
                new AlertDialog.Builder(this)
                        .setTitle( getString(R.string.dialog_add_game_to_wishlist_title) )
                        .setMessage( getString(R.string.dialog_add_game_to_wishlist_text) )

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                WishlistManager wishlist =
                                        WishlistManager.getInstance(getApplicationContext());
                                wishlist.add(mGame);

                                WishListRepository repository = WishListRepository.getInstance();
                                repository.add(mGame);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;

            case R.id.action_remove_game:
                new AlertDialog.Builder(this)
                        .setTitle( getString(R.string.dialog_remove_game_from_wishlist_title) )
                        .setMessage( getString(R.string.dialog_remove_game_from_wishlist_text) )

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                WishlistManager wishlist =
                                        WishlistManager.getInstance(getApplicationContext());
                                wishlist.removeGameFromWishlist(mGame);

                                WishListRepository repository = WishListRepository.getInstance();
                                repository.remove(mGame);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO: add documentation
    private static class DownloadGame extends AsyncTask<String, Integer, Game> {

        private final WeakReference<GamePageActivity> mGamePageActivity;

        private DownloadGame(GamePageActivity gamePageActivity) {
            mGamePageActivity = new WeakReference<>(gamePageActivity);
        }

        @Override
        protected Game doInBackground(String... strings) {
            Gamestop gamestop = new Gamestop();
            return gamestop.downloadGame(strings[0]);
        }

        @Override
        protected void onPostExecute(Game game) {
            GamePageActivity activity = mGamePageActivity.get();
            if (activity != null) {
                activity.mGame = game;
                activity.updateUI(game);
            }
        }

    }

    // TODO: add documentation
    private void updateUI(Game game) {

        // set action bar
        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        // init game page UI
        setGameImages(game);
        setGameData(game);
        setGameRating(game);
        setGamePrices(game);
        setGamePromo(game);

        // make game page UI visible
        LinearLayout linearLayout = findViewById(R.id.game_page_container);
        linearLayout.setVisibility(View.VISIBLE);
    }

    // TODO: add documentation
    private void setGameData(Game game) {

        // Get views
        TextView title = findViewById(R.id.title);
        TextView publisher = findViewById(R.id.publisher);
        TextView platform = findViewById(R.id.platform);
        TextView genres = findViewById(R.id.genres);
        TextView releaseDate = findViewById(R.id.releaseDate);
        TextView players = findViewById(R.id.players);
        TextView officialSite = findViewById(R.id.officialSite);
        TextView description = findViewById(R.id.description);
        LinearLayout validForPromotions = findViewById(R.id.valid_for_promotions);

        LinearLayout genresContainer = findViewById(R.id.genres_container);
        LinearLayout releaseDateContainer = findViewById(R.id.release_date_container);
        LinearLayout playersContainer = findViewById(R.id.players_container);
        LinearLayout officialSiteContainer = findViewById(R.id.official_site_container);

        // Set data
        title.setText( game.getTitle() );
        publisher.setText( game.getPublisher() );
        platform.setText( game.getPlatform() );

        if (game.hasGenres()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String genre : game.getGenres()) {
                stringBuilder.append(genre).append("/");
            }
            stringBuilder.deleteCharAt( stringBuilder.length() - 1 );
            genres.setText(stringBuilder);
        } else {
            genresContainer.setVisibility(View.GONE);
        }

        if (game.hasReleaseDate()) {
            releaseDate.setText( game.getReleaseDate() );
        } else {
            releaseDateContainer.setVisibility(View.GONE);
        }

        if (game.hasPlayers()) {
            playersContainer.setVisibility(View.VISIBLE);
            players.setText( game.getPlayers() );
        }

        if (game.hasOfficialWebSite()) {
            String href = game.getOfficialWebSite();
            String domain = href.split("/")[2];
            Spanned link = Html.fromHtml("<a href='" + href + "'>" + domain + "</a>");

            officialSite.setMovementMethod( LinkMovementMethod.getInstance() );
            officialSite.setText(link);
            officialSiteContainer.setVisibility(View.VISIBLE);
        }

        if (game.isValidForPromotions()) {
            validForPromotions.setVisibility(View.VISIBLE);
        }

        // TODO: links in the description don't do anything
        if (game.hasDescription()) {
            String html = game.getDescription();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.setMovementMethod( LinkMovementMethod.getInstance() );
                description.setText( Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY) );
            } else {
                description.setMovementMethod( LinkMovementMethod.getInstance() );
                description.setText( Html.fromHtml(html) );
            }
        }
    }

    // TODO: add documentation
    private void setGameRating(Game game) {

        if (game.hasPegi()) {

            HorizontalScrollView pegiContainer = findViewById(R.id.pegi_container);
            pegiContainer.setVisibility(View.VISIBLE);

            for (String type : game.getPegi()) {
                switch (type) {
                    case "pegi3":
                        findViewById(R.id.pegi_pegi3).setVisibility(View.VISIBLE);
                        break;
                    case "pegi7":
                        findViewById(R.id.pegi_pegi7).setVisibility(View.VISIBLE);
                        break;
                    case "pegi12":
                        findViewById(R.id.pegi_pegi12).setVisibility(View.VISIBLE);
                        break;
                    case "pegi16":
                        findViewById(R.id.pegi_pegi16).setVisibility(View.VISIBLE);
                        break;
                    case "pegi18":
                        findViewById(R.id.pegi_pegi18).setVisibility(View.VISIBLE);
                        break;
                    case "bad-language":
                        findViewById(R.id.pegi_bad_language).setVisibility(View.VISIBLE);
                        break;
                    case "discrimination":
                        findViewById(R.id.pegi_discrimination).setVisibility(View.VISIBLE);
                        break;
                    case "drugs":
                        findViewById(R.id.pegi_drugs).setVisibility(View.VISIBLE);
                        break;
                    case "fear":
                        findViewById(R.id.pegi_fear).setVisibility(View.VISIBLE);
                        break;
                    case "gambling":
                        findViewById(R.id.pegi_gambling).setVisibility(View.VISIBLE);
                        break;
                    case "online":
                        findViewById(R.id.pegi_online).setVisibility(View.VISIBLE);
                        break;
                    case "sex":
                        findViewById(R.id.pegi_sex).setVisibility(View.VISIBLE);
                        break;
                    case "violence":
                        findViewById(R.id.pegi_violence).setVisibility(View.VISIBLE);
                        break;
                }
            }
        }

    }

    // TODO: add documentation
    private void setGameImages(Game game) {

        // Get views
        ImageView cover = findViewById(R.id.cover);
        LinearLayout gallery = findViewById(R.id.gallery);
        LinearLayout galleryContainer = findViewById(R.id.gallery_container);

        // set listener to the cover
        Bundle coverBundle = new Bundle();
        coverBundle.putParcelable("cover", game.getCover());
        cover.setTag(coverBundle);

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(v);
            }
        });

        // load cover in the view
        Picasso.get().load(game.getCover()).into(cover);

        // Add images to gallery
        if (game.hasGallery()) {

            galleryContainer.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            ArrayList<Uri> images = (ArrayList<Uri>) game.getGallery();

            // insert all the images in the gallery
            for (int i = 0; i < images.size(); ++i) {

                // create view
                ImageView imageView = new ImageView(this);

                // init view
                imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(layoutParams);

                // last image in the gallery doesn't have margin
                if (i != images.size()-1) {
                    imageView.setPadding(0, 0, (int) Util.convertDpToPx(this, 8), 0);
                }

                // set tag
                Bundle galleryBundle = new Bundle();
                galleryBundle.putParcelableArrayList("gallery", images);
                galleryBundle.putInt("position", i);
                imageView.setTag(galleryBundle);

                // set listener
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGallery(v);
                    }
                });

                // load image in the view
                Picasso.get().load( images.get(i) ).into(imageView);

                // add view to gallery
                gallery.addView(imageView);
            }
        }
    }

    // TODO: add documentation
    private void setGamePrices(GamePreview game) {

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup parent = findViewById(R.id.section_prices);

        // set new prices
        if (game.hasNewPrice()) {

            // create the container
            ViewGroup container = (ViewGroup) inflater
                    .inflate(R.layout.partial_price_container, parent, false);

            // add prices to the container
            setPrice(container, game.getNewPrice(), R.string.new_price);
            if (game.hasOlderNewPrices()) {
                setOldPrices(container, game.getOlderNewPrices());
            }

            // add the container to the parent
            parent.addView(container);
        }

        // set old prices
        if (game.hasUsedPrice()) {

            // create the container
            LinearLayout container = (LinearLayout) inflater
                    .inflate(R.layout.partial_price_container, parent, false);

            // add prices to the container
            setPrice(container, game.getUsedPrice(), R.string.used_price);
            if (game.hasOlderUsedPrices()) {
                setOldPrices(container, game.getOlderUsedPrices());
            }

            // add the container to the parent
            parent.addView(container);
        }

        // set digital prices
        if (game.hasDigitalPrice()) {

            // create the container
            LinearLayout container = (LinearLayout) inflater
                    .inflate(R.layout.partial_price_container, parent, false);

            // add prices to the container
            setPrice(container, game.getDigitalPrice(), R.string.digital_price);
            if (game.hasOlderDigitalPrices()) {
                setOldPrices(container, game.getOlderDigitalPrices());
            }

            // add the container to the parent
            parent.addView(container);
        }

        // set preorder prices
        if (game.hasPreorderPrice()) {

            // create the container
            LinearLayout container = (LinearLayout) inflater
                    .inflate(R.layout.partial_price_container, parent, false);

            // add prices to the container
            setPrice(container, game.getPreorderPrice(), R.string.preorder_price);
            if (game.hasOlderPreorderPrices()) {
                setOldPrices(container, game.getOlderPreorderPrices());
            }

            // add the container to the parent
            parent.addView(container);
        }
    }

    // TODO: add documentation
    private void setPrice(ViewGroup container, Double price, int priceType) {

        DecimalFormat df = new DecimalFormat("#.00");

        // create view
        TextView priceView = new TextView(this);

        // set view parameters
        priceView.append( getString(priceType) );
        priceView.append( df.format(price) );
        priceView.append( getString(R.string.currency) );
        priceView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        priceView.setTextColor(Color.WHITE);
        priceView.setTypeface(Typeface.DEFAULT_BOLD);

        // add view to the container
        container.addView(priceView);
    }

    // TODO: add documentation
    private void setOldPrices(ViewGroup container, List<Double> oldPrices) {

        DecimalFormat df = new DecimalFormat("#.00");

        for (Double oldPrice : oldPrices) {

            // create view
            TextView oldPricesView = new TextView(this);

            // set view parameters
            oldPricesView.append( df.format(oldPrice) );
            oldPricesView.append( getString(R.string.currency) );
            oldPricesView.setPadding( (int) Util.convertDpToPx(this, 7), 0, 0, 0);
            oldPricesView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            oldPricesView.setTextColor(Color.WHITE);
            oldPricesView.setTypeface(Typeface.DEFAULT_BOLD);
            oldPricesView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            // add view to the container
            container.addView(oldPricesView);
        }
    }

    // TODO: add documentation
    private void setGamePromo(Game game) {

        LayoutInflater inflater = getLayoutInflater();

        // Get Views
        LinearLayout promoContainer = findViewById(R.id.promo_container);

        // TODO: set URL to promo Message
        // TODO: check if message is available
        if (game.hasPromo()) {
            for (Promo promo : game.getPromo()) {

                // Create a promo view
                View promoView = inflater.inflate(R.layout.partial_section_promo,
                        promoContainer, false);

                // init promo view
                TextView promoHeader = promoView.findViewById(R.id.promo_header);
                TextView promoValidity = promoView.findViewById(R.id.promo_validity);
                TextView promoMessage = promoView.findViewById(R.id.promo_message);

                promoHeader.setText( promo.getHeader() );
                promoValidity.setText( promo.getSubHeader() );

                if (promo.hasFindMoreMessage()) {
                    String href = promo.getFindMoreUrl();
                    String message = promo.getFindMoreMessage();
                    Spanned link = Html.fromHtml("<a href='" + href + "'>" + message + "</a>");

                    promoMessage.setMovementMethod( LinkMovementMethod.getInstance() );
                    promoMessage.setText(link);
                    promoMessage.setVisibility(View.VISIBLE);
                }

                // add promo to promoContainer
                promoContainer.addView(promoView);
            }

            promoContainer.setVisibility(View.VISIBLE);
        }
    }

    // TODO: add documentation
    private void openGallery(View view) {

        Bundle bundle = (Bundle) view.getTag();

        int position = bundle.getInt("position");
        Uri cover = bundle.getParcelable("cover");
        ArrayList<Uri> gallery = bundle.getParcelableArrayList("gallery");

        // create and start intent
        Intent intent = new Intent(this, GalleryActivity.class);

        if (cover == null) {
            intent.putParcelableArrayListExtra("images", gallery);
            intent.putExtra("position", position);
        } else {
            ArrayList<Uri> coverArray = new ArrayList<>();
            coverArray.add(cover);
            intent.putParcelableArrayListExtra("images", coverArray);
            intent.putExtra("position", 0);
        }

        startActivity(intent);
    }

}
