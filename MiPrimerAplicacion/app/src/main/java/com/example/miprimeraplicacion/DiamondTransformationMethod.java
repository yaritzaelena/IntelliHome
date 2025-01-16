package com.example.miprimeraplicacion;
import android.text.method.PasswordTransformationMethod;
import android.view.View;

public class DiamondTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new DiamondCharSequence(source);
    }

    private static class DiamondCharSequence implements CharSequence {
        private final CharSequence source;

        DiamondCharSequence(CharSequence source) {
            this.source = source;
        }

        @Override
        public int length() {
            return source.length();
        }

        @Override
        public char charAt(int index) {
            return '◆'; // Aquí se cambia el carácter por un rombo
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new DiamondCharSequence(source.subSequence(start, end));
        }
    }
}
