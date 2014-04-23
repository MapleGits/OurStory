package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class MapleDataTool {

    public static String getString(MapleData data) {
        return (String) data.getData();
    }

    public static String getString(MapleData data, String def) {
        if ((data == null) || (data.getData() == null)) {
            return def;
        }
        if ((data.getType() == MapleDataType.STRING) || ((data.getData() instanceof String))) {
            return (String) data.getData();
        }
        return String.valueOf(getInt(data));
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        return getString((data == null) || (data.getChildByPath(path) == null) ? null : data.getChildByPath(path), def);
    }

    public static double getDouble(MapleData data) {
        return ((Double) data.getData()).doubleValue();
    }

    public static float getFloat(MapleData data) {
        return ((Float) data.getData()).floatValue();
    }

    public static float getFloat(MapleData data, float def) {
        if ((data == null) || (data.getData() == null)) {
            return def;
        }
        return ((Float) data.getData()).floatValue();
    }

    public static int getInt(MapleData data) {
        return ((Integer) data.getData()).intValue();
    }

    public static long getLong(MapleData data) {
        return ((Long) data.getData()).longValue();
    }

    public static int getInt(MapleData data, int def) {
        if ((data == null) || (data.getData() == null)) {
            return def;
        }
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        }
        if (data.getType() == MapleDataType.SHORT) {
            return Integer.valueOf(((Short) data.getData()).shortValue()).intValue();
        }
        return ((Integer) data.getData()).intValue();
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getIntConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            if (getString(data).equals("??????")) {
                return 1;
            }
            return Integer.parseInt(getString(data));
        }
        return getInt(data);
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            if (getString(d).equals("??????")) {
                return 1;
            }
            return Integer.parseInt(getString(d));
        }
        return getInt(d);
    }

    public static long getLongConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return 0L;
        }
        if (d.getType() == MapleDataType.STRING) {
            if (getString(d).equals("??????")) {
                return 1L;
            }
            return Long.parseLong(getString(d));
        }
        return getLong(d);
    }

    public static int getInt(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        return getInt(data.getChildByPath(path), def);
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        return getIntConvert(data.getChildByPath(path), def);
    }

    public static int getIntConvert(MapleData d, int def) {
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            String dd = getString(d);
            if (dd.endsWith("%")) {
                dd = dd.substring(0, dd.length() - 1);
            }
            try {
                return Integer.parseInt(dd);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return getInt(d, def);
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        return (Point) data.getData();
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}