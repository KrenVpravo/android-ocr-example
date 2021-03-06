package com.two_two.android_ocr_example.ui.ocr;

import android.graphics.Bitmap;

import com.two_two.android_ocr_example.data.ImageRepository;
import com.two_two.android_ocr_example.di.DependencyProvider;
import com.two_two.android_ocr_example.domain.LanguageCodeHelper;
import com.two_two.android_ocr_example.domain.tess.TessInitializator;
import com.two_two.android_ocr_example.domain.tess.TessRecognizer;
import com.two_two.android_ocr_example.utils.Constants;

/**
 * @author Dmitry Borodin on 2017-01-01.
 */

public class OcrActivityPresenter implements OcrActivityContract.Presenter {

    private OcrActivityContract.View view;
    private ImageRepository imageRepository;

    public OcrActivityPresenter(OcrActivityContract.View view) {
        this.view = view;
        imageRepository = DependencyProvider.getImageRepository();
    }

    @Override
    public void onViewResumed() {
        setAndAnalyzeRecentImage();
    }

    private void setAndAnalyzeRecentImage() {
        startInitOcrEngine();
        view.showAnalyzedImage(imageRepository.getPictureBitmapForOcr());
    }

    @Override
    public void onViewDestroyed() {
        view = null;
    }

    @Override
    public void onNewImageTaken(Bitmap newimage) {
        imageRepository.setMostRecentPicture(newimage);
        setAndAnalyzeRecentImage();
    }

    private void startInitOcrEngine() {
        LanguageCodeHelper languageCodeHelper = DependencyProvider.getLanguageCodeHelper();

        String languageCode = Constants.DEFAULT_LANGUAGE_CODE;
        String languageName = languageCodeHelper.getOcrLanguageName(languageCode);
        initOcrEngine(languageCode, languageName);
    }

    /**
     * @param languageCode Three-letter ISO 639-3 language code for OCR
     * @param languageName Name of the language for OCR, for example, "English"
     */
    private void initOcrEngine(String languageCode, String languageName) {
        if (view != null) {
            view.showInitTessProgressBar();
        }
        TessInitializator initializator = DependencyProvider.getTessInitializator();
        initializator.initTessOcrEngine(languageCode, languageName, new TessInitializator.Callback() {
            @Override
            public void onError(String message) {
                if (view != null) {
                    view.dismissProgressBar();
                    view.showError(message);
                }
            }

            @Override
            public void onInitialized() {
                if (view != null) {
                    view.dismissProgressBar();
                    recognizePicture();
                }
            }
        });
    }


    private void recognizePicture() {
        if (view != null) {
            view.showRecognizingProgressBar();
        }
        Bitmap picture = imageRepository.getPictureBitmapForOcr();
        DependencyProvider.getTessRecognizer().inspectFromBitmapAsync(picture, new TessRecognizer.RecognizedCallback() {
            @Override
            public void onRecognized(String result) {
                if (view != null) {
                    view.dismissProgressBar();
                    view.showRecognizedText(result);
                }
            }
        });
    }
}
