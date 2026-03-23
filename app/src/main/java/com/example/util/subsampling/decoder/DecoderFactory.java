/*
 * Derived from davemorrissey/subsampling-scale-image-view
 * https://github.com/davemorrissey/subsampling-scale-image-view
 *
 * Original work Copyright 2018 David Morrissey
 * Licensed under the Apache License, Version 2.0.
 *
 * Modified for this project:
 * - Package names were relocated for local integration.
 * - The source was embedded directly into the app module.
 */
package com.example.util.subsampling.decoder;
import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface for {@link ImageDecoder} and {@link ImageRegionDecoder} factories.
 * @param <T> the class of decoder that will be produced.
 */
public interface DecoderFactory<T> {

    /**
     * Produce a new instance of a decoder with type {@link T}.
     * @return a new instance of your decoder.
     * @throws IllegalAccessException if the factory class cannot be instantiated.
     * @throws InstantiationException if the factory class cannot be instantiated.
     * @throws NoSuchMethodException if the factory class cannot be instantiated.
     * @throws InvocationTargetException if the factory class cannot be instantiated.
     */
    @NonNull T make() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;

}
