package dev.arubik.realmcraft.Api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import dev.arubik.realmcraft.realmcraft;
import dev.arubik.realmcraft.Handlers.RealMessage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Utils {
    private static Random random = new Random();

    public static boolean Chance(double number, int range) {
        if (number == 0)
            return false;
        if (number >= range)
            return true;
        double random = Math.random() * (range + 1);
        return random <= number;
    }

    public static String round(double number) {
        return String.valueOf(Math.round(number));
    }

    public static int randomNumer(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String round(double number, int decimals) {
        // decimals are the number of decimal places
        return String.format("%." + decimals + "f", number);
    }

    public static Double roundDouble(double number, int decimals) {
        // round using math
        return Math.round(number * Math.pow(10, decimals)) / Math.pow(10, decimals);

    }

    public static ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Throwable e) {
            // throw new IOException("Unable to decode class type.", e);
        }
        return new ItemStack[] { RealNBT.Empty };
    }

    public static String itemStackArrayToBase64(ItemStack[] objects) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(objects.length);

            // Save every element in the list
            for (int i = 0; i < objects.length; i++) {
                dataOutput.writeObject(objects[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static String itemStackArrayToBase64(ItemStack object) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(object);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public static boolean checkPermission(Player p, String permission) {
        String fullPerm = "realmcraft." + permission;
        return p.hasPermission(fullPerm);
    }

    public static boolean checkPermission(CommandSender p, String permission) {
        String fullPerm = "realmcraft." + permission;
        return p.hasPermission(fullPerm);
    }

    public static boolean checkPermissions(Player p, String... permission) {
        for (String perm : permission) {
            if (checkPermission(p, perm)) {
                return true;
            }
        }
        return false;
    }

    public static String getPermission(String permission) {
        return "realmcraft." + permission;
    }

    public static Integer doubleToInt(Double d) {
        return d.intValue();
    }

    public static Integer floatToInt(Float f) {
        return f.intValue();
    }

    /*
     * public static String colors(String text)
     * 
     * Convert from legacy format &c to new format <red>
     * 
     * @param text The text to convert
     * 
     * @return The converted text
     */
    public static String colors(String text) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        return MiniMessage.miniMessage().serialize(serializer.deserialize(text));
    }

    public static int random(int min, int max) {
        if (min == max)
            return min;
        if (min > max) {
            return random.nextInt(min - max + 1) + max;
        }
        return random.nextInt(max - min + 1) + min;
    }

    public static double random(double min, double max) {
        if (min == max)
            return min;
        if (min > max) {
            return random.nextDouble(min - max + 1) + max;
        }
        return roundDouble(random.nextDouble(max - min + 1) + min, 2);
    }

    public static Integer[] removeInt(Integer[] array, Integer value) {
        Integer[] newArray = new Integer[array.length - 1];
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != value) {
                newArray[index] = array[i];
                index++;
            }
        }
        return newArray;
    }

    public static <T> T[] from(T... t) {
        return t;
    }

    public static List<String> removeString(List<String> array, String value) {
        List<String> newArray = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            if (!array.get(i).equals(value)) {
                newArray.add(array.get(i));
            }
        }
        return newArray;
    }

    public static Integer[] randomizeArrayOrder(Integer[] array) {
        Integer[] newArray = new Integer[array.length];
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        for (int i = 0; i < array.length; i++) {
            int index = random(0, list.size() - 1);
            newArray[i] = list.get(index);
            list.remove(index);
        }
        return newArray;
    }

    @Nullable
    public static <T> T randomFromList(List<T> list) {
        if (list.isEmpty())
            return null;
        int index = random(0, list.size() - 1);
        return list.get(index);
    }

    public static void Delay(Runnable object, int slowDuration) {
        Bukkit.getScheduler().runTaskLater(realmcraft.getInstance(), object, slowDuration);
    }

    public static ArrayList<Class<?>> findAllClassesUsingClassLoader(String pkgName) {
        final String pkgPath = pkgName.replace('.', '/');
        URI pkg;
        try {
            pkg = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(pkgPath)).toURI();
            final ArrayList<Class<?>> allClasses = new ArrayList<Class<?>>();

            Path root;
            if (pkg.toString().startsWith("jar:")) {
                try {
                    root = FileSystems.getFileSystem(pkg).getPath(pkgPath);
                } catch (final FileSystemNotFoundException e) {
                    root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath);
                }
            } else {
                root = Paths.get(pkg);
            }

            final String extension = ".class";
            try (final Stream<Path> allPaths = Files.walk(root)) {
                allPaths.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        final String path = file.toString().replace('/', '.');
                        final String name = path.substring(path.indexOf(pkgName), path.length() - extension.length());
                        allClasses.add(Class.forName(name));
                    } catch (final ClassNotFoundException | StringIndexOutOfBoundsException ignored) {
                    }
                });
            }
            return allClasses;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
