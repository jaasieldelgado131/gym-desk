package dev.gimnasio.sistemagimnasio.application.usecase.settings;

import dev.gimnasio.sistemagimnasio.domain.settings.GymSettings;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Reconoce la imagen por sus primeros bytes, no por el nombre del archivo. */
final class LogoFormat {
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int SVG_PROBE_BYTES = 1024;

    private LogoFormat() {
    }

    static boolean isAccepted(byte[] image) {
        if (image == null || image.length == 0 || image.length > GymSettings.MAX_LOGO_BYTES) {
            return false;
        }
        return startsWith(image, 0, PNG)
                || startsWith(image, 0, JPG)
                || (startsWith(image, 0, RIFF) && startsWith(image, 8, WEBP))
                || isSvg(image);
    }

    private static boolean startsWith(byte[] image, int offset, byte[] signature) {
        return image.length >= offset + signature.length
                && Arrays.equals(image, offset, offset + signature.length, signature, 0, signature.length);
    }

    private static boolean isSvg(byte[] image) {
        String head = new String(image, 0, Math.min(image.length, SVG_PROBE_BYTES), StandardCharsets.UTF_8);
        if (head.startsWith("﻿")) {
            head = head.substring(1);
        }
        head = head.stripLeading();
        return head.startsWith("<svg") || (head.startsWith("<?xml") && head.contains("<svg"));
    }
}
