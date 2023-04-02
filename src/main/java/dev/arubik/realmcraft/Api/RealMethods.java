package dev.arubik.realmcraft.Api;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealMethods {

    enum Methods {
        RAN("random(#1,#2)"),
        MAX("max(#1,#2)"),
        MIN("min(#1,#2)"),
        AVG("average(#1,#2)"),
        SUM("sum(#1,#2)"),
        SUB("subtract(#1,#2)"),
        SPREAD("spread(#1,#2)"),
        BASE("base(#1,#2,#3)"),
        ADDPERCENT("addpercent(#1,#2)"),
        TAKEPERCENT("takepercent(#1,#2)");

        private final String text;

        Methods(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public String format(String... args) {
            String result = text;
            for (int i = 0; i < args.length; i++) {
                result = result.replace("#" + (i + 1), args[i]);
            }
            return result;
        }

        public Object parse(String... args) {
            return parseMethod(format(args));
        }
    }

    public static Random rand = new Random();

    public static Object parseMethod(Object obj) {
        if (obj instanceof Integer || obj instanceof Double) {
            return obj;
        }
        String text = obj.toString();
        String pattern = "([a-z]+)\\((\\d+(\\.\\d*)?)\\s*,\\s*(\\d+(\\.\\d*)?)\\)";
        Matcher m = Pattern.compile(pattern).matcher(text);

        while (m.find()) {
            String funcName = m.group(1);
            if (funcName.equalsIgnoreCase("random") || funcName.equalsIgnoreCase("ran")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = rand.nextDouble() * (arg2 - arg1) + arg1;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("max")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = Math.max(arg1, arg2);
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("min")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = Math.min(arg1, arg2);
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("average") || funcName.equalsIgnoreCase("avg")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = (arg1 + arg2) / 2;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("sum")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = arg1 + arg2;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("subtract") || funcName.equalsIgnoreCase("sub")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = arg1 - arg2;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("spread")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = rand.nextDouble() * (arg2 - arg1) + arg1;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("base")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double arg3 = Double.parseDouble(m.group(6));
                double result = arg1 + (arg2 - arg1) * arg3;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("addpercent")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = arg1 + arg1 * arg2 / 100;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            } else if (funcName.equalsIgnoreCase("takepercent")) {
                double arg1 = Double.parseDouble(m.group(2));
                double arg2 = Double.parseDouble(m.group(4));
                double result = arg1 - arg1 * arg2 / 100;
                text = text.replaceFirst(Pattern.quote(m.group(0)), String.format("%.2f", result));
            }

        }
        return text;
    }
}
