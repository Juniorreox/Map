/*
 * Copyright (c) 2021 by k3b.
 *
 * This file is part of of k3b-geoHelper library and LocationMapViewer.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package de.k3b.geo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.k3b.geo.api.IGeoPointInfo;
import de.k3b.io.GeoConfig;

/* TODO: move to Geohelper */
public class GeoLoadService {
    /**
     * @return list of found existing geo-filenames (without path) below dir
     * */
    @NonNull
    public static List<String> getGeoFiles(File dir, Map<String, File> name2file) {
        final List<String> found = new ArrayList<>();
        File[] files = listFiles(dir);
        if (files != null) {
            for (File file : files) {
                String name = getName(file);
                String nameLower = name.toLowerCase();
                name2file.put(nameLower, file);
                if (isGeo(nameLower)) {
                    found.add(name);
                }
            }
        }
        return found;
    }

    public static File getFirstGeoFile(File dir) {
        HashMap<String, File> name2file = new HashMap<>();
        List<String> files = getGeoFiles(dir, name2file);
        if (!files.isEmpty()) return name2file.get(files.get(0));
        return null;
    }

    public static boolean isGeo(String nameLower) {
        return GeoConfig.isOneOf(nameLower, GeoConfig.EXT_ALL);
    }

    public static boolean iszip(String nameLower) {
        return GeoConfig.isOneOf(nameLower, GeoConfig.EXT_ALL_ZIP);
    }

    public static String convertSymbol(final IGeoPointInfo aGeoPoint, final File dir, final Map<String, File> name2file) {
        String symbol = aGeoPoint != null ? aGeoPoint.getSymbol() : null;
        if (symbol != null && !symbol.contains(":") && symbol.contains(".")) {
            symbol = symbol.toLowerCase();
            File doc = name2file.get(symbol);
            if (doc == null && symbol.contains("/")) {
                doc = addFiles(dir, symbol.split("/"), name2file);
            }
            if (doc != null) return getUri(doc);
        }
        return null;
    }

    private static File addFiles(File dir, String[] pathElements, Map<String, File> name2file) {
        File currentdir = dir;
        StringBuilder path = new StringBuilder();
        File doc = null;
        int last = pathElements.length - 1;
        for (int i = 0; i <= last; i++) {
            if (path.length() > 0) path.append("/");
            String parentPath = path.toString();
            path.append(pathElements[i]);
            String pathLowerCase = path.toString();
            doc = name2file.get(pathLowerCase);
            if (doc == null && i <= last) {
                File[] children = listFiles(currentdir);
                if (children != null) {
                    for (File child : children) {
                        name2file.put(parentPath  + getName(child).toLowerCase(), child);
                    }
                    doc = name2file.get(pathLowerCase);
                }
            }
            currentdir = doc;
        }
        return doc;
    }

    private static String getName(@NonNull File file) {
        return file.getName();
    }

    // ----- File api abstractions
    @Nullable
    private static File[] listFiles(@Nullable File dir) {
        if (isExistingDirectory(dir)) {
            return dir.listFiles();
        }
        return null;
    }

    private static boolean isExistingDirectory(@Nullable File dir) {
        return dir != null && dir.exists() && dir.isDirectory();
    }

    private static String getUri(File doc) {
        return doc.getAbsolutePath();
    }

    // ----- File api
    public static void closeSilently(java.io.Closeable inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}