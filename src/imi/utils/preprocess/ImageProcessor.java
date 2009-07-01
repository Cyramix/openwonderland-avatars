/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.utils.preprocess;

import imi.utils.FileUtils;
import java.io.File;

/**
 * This class process an image and then normalizes the RGB channels to lighten/
 * whiten the images.
 * @author ptruong
 */
public class ImageProcessor {

    private ImageProcessor(Builder builder) {
        FileUtils.processImages(builder.sourceDirectory);
    }

    public static class Builder {
        private File sourceDirectory = new File("./assets/source/");

        public Builder() {
        }

        public Builder source(File source) {
            this.sourceDirectory    = source;
            return this;
        }
        public ImageProcessor build() {
            return new ImageProcessor(this);
        }
    }

    public static void main(String[] args) {
        new ImageProcessor.Builder().build();
    }
}
