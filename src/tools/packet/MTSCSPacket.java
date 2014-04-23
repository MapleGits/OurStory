package tools.packet;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.cashshop.handler.cashinformation;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import server.CashItemFactory;
import server.CashItemInfo;
import server.CashItemInfo.CashModInfo;
import server.CashShop;
import server.MTSStorage;
import server.MTSStorage.MTSItemInfo;
import server.MapleStorage;
import tools.HexTool;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

public class MTSCSPacket {

    private static byte Operation_Code = 111;
   
    public static byte[] warpCS(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());

        PacketHelper.addCharacterInfo(mplew, c.getPlayer());

        mplew.write(HexTool.getByteArrayFromHexString("18 BD 63 02 22 89 40 00 04 00 00 00 00 00 00 00 75 96 8F 00 00 00 00 00 76 96 8F 00 6A 04 00 00 77 96 8F 00 00 00 00 00 78 96 8F 00 00 00 00 00 01 00 00 00 00 00 00 00 0F 00 00 00 4B 6D 54 00 07 00 00 00 75 31 31 01 76 31 31 01 77 31 31 01 78 31 31 01 79 31 31 01 7A 31 31 01 7B 31 31 01 53 6D 54 00 06 00 00 00 42 E3 F5 05 43 E3 F5 05 44 E3 F5 05 45 E3 F5 05 46 E3 F5 05 47 E3 F5 05 55 6D 54 00 0F 00 00 00 A8 E2 F5 05 A9 E2 F5 05 AA E2 F5 05 AB E2 F5 05 AC E2 F5 05 AD E2 F5 05 AE E2 F5 05 AF E2 F5 05 B0 E2 F5 05 B1 E2 F5 05 B2 E2 F5 05 B3 E2 F5 05 B4 E2 F5 05 B5 E2 F5 05 B6 E2 F5 05 5D 6D 54 00 05 00 00 00 3D 63 3D 01 3E 63 3D 01 3F 63 3D 01 40 63 3D 01 41 63 3D 01 4E 6D 54 00 05 00 00 00 AC E1 F5 05 AD E1 F5 05 AE E1 F5 05 AF E1 F5 05 B0 E1 F5 05 56 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 5E 6D 54 00 03 00 00 00 91 F6 41 01 92 F6 41 01 93 F6 41 01 5F 6D 54 00 05 00 00 00 71 2F 31 01 72 2F 31 01 73 2F 31 01 74 2F 31 01 75 2F 31 01 60 6D 54 00 04 00 00 00 6D 2F 31 01 6E 2F 31 01 6F 2F 31 01 70 2F 31 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 59 6D 54 00 04 00 00 00 4E A1 98 00 4F A1 98 00 50 A1 98 00 51 A1 98 00 62 6D 54 00 06 00 00 00 C3 C3 59 08 C4 C3 59 08 C5 C3 59 08 C6 C3 59 08 C7 C3 59 08 C8 C3 59 08 4A 6D 54 00 05 00 00 00 6C 64 3D 01 6D 64 3D 01 6E 64 3D 01 6F 64 3D 01 70 64 3D 01 5A 6D 54 00 10 00 00 00 13 E1 F5 05 04 E1 F5 05 05 E1 F5 05 06 E1 F5 05 07 E1 F5 05 08 E1 F5 05 09 E1 F5 05 0A E1 F5 05 0B E1 F5 05 0C E1 F5 05 0D E1 F5 05 0E E1 F5 05 0F E1 F5 05 10 E1 F5 05 11 E1 F5 05 12 E1 F5 05 04 EC 6F 11 CC E9 6F 11 10 E7 6F 11 94 E7 6F 11 D8 E4 6F 11 8C E6 6F 11 08 E6 6F 11 30 E5 6F 11 AC E4 6F 11 90 ED 6F 11 D0 E3 6F 11 30 EC 6F 11 24 EA 6F 11 F8 E9 6F 11 A8 EA 6F 11 50 EA 6F 11 80 E4 6F 11 1C E9 6F 11 C4 E8 6F 11 38 ED 6F 11 18 E8 6F 11 68 E7 6F 11 3C E7 6F 11 E4 E6 6F 11 DC E5 6F 11 54 EB 6F 11 84 E5 6F 11 28 E4 6F 11 34 E6 6F 11 48 E9 6F 11 B8 E6 6F 11 98 E8 6F 11 88 EC 6F 11 74 E9 6F 11 5C EC 6F 11 F0 E1 6F 11 78 E3 6F 11 90 E0 6F 11 64 E0 6F 11 38 E0 6F 11 E0 DF 6F 11 B4 DF 6F 11 60 DF 6F 11 1C E2 6F 11 08 DF 6F 11 DC DE 6F 11 B0 DE 6F 11 58 DE 6F 11 2C DE 6F 11 00 DE 6F 11 A8 DD 6F 11 7C DD 6F 11 50 DD 6F 11 24 DD 6F 11 F8 DC 6F 11 CC DC 6F 11 80 D9 6F 11 44 8D FD 05 9C 8D FD 05 EC 8C FD 05 18 8D FD 05 20 8E FD 05 4C 8E FD 05 C8 8D FD 05 F4 8D FD 05 78 8E FD 05 A4 8E FD 05 70 8D FD 05 D0 8E FD 05 FC 8E FD 05 80 8F FD 05 24 D2 45 0A 50 D2 45 0A 28 8F FD 05 54 8F FD 05 D4 DD 6F 11 7C D2 45 0A A8 D2 45 0A D4 D2 45 0A 84 DE 6F 11 00 D3 45 0A 2C D3 45 0A 58 D3 45 0A 34 DF 6F 11 84 D3 45 0A B0 D3 45 0A DC D3 45 0A 0C E0 6F 11 08 D4 45 0A 34 D4 45 0A 4C E3 6F 11 BC E0 6F 11 20 E3 6F 11 F4 E2 6F 11 C8 E2 6F 11 6C E1 6F 11 9C E2 6F 11 48 E2 6F 11 0C 6F 52 0E 38 6F 52 0E 04 D4 6F 11 64 6F 52 0E 90 6F 52 0E 30 D4 6F 11 18 D7 6F 11 BC 6F 52 0E E8 6F 52 0E 88 D4 6F 11 14 70 52 0E 40 70 52 0E 5C D4 6F 11 C8 D7 6F 11 D0 D8 6F 11 6C 70 52 0E 98 70 52 0E B4 D4 6F 11 C4 70 52 0E F0 70 52 0E 38 D5 6F 11 20 D8 6F 11 1C 71 52 0E 48 71 52 0E 64 D5 6F 11 74 71 52 0E A0 71 52 0E 0C D5 6F 11 44 D7 6F 11 FC D8 6F 11 A4 D8 6F 11 F4 71 52 0E 20 72 52 0E E0 D4 6F 11 4C 72 52 0E 78 72 52 0E 90 D5 6F 11 6C D6 6F 11 A4 72 52 0E FC 72 52 0E E8 D5 6F 11 D0 72 52 0E 28 73 52 0E BC D5 6F 11 9C D7 6F 11 54 D9 6F 11 54 73 52 0E 80 73 52 0E 14 D6 6F 11 AC 73 52 0E D8 73 52 0E 98 D6 6F 11 F4 D7 6F 11 04 74 52 0E 30 74 52 0E EC D6 6F 11 60 D4 45 0A 88 74 52 0E 8C D4 45 0A B8 D4 45 0A 5C 74 52 0E 40 D6 6F 11 70 D7 6F 11 78 D8 6F 11 4C D8 6F 11 28 D9 6F 11 D4 EA 6F 11 00 EB 6F 11 48 00 01 49 D5 73 1C 0A 30 02 00 00 23 00 70 00 72 00 6F 00 70 00 25 00 20 00 63 00 68 00 61 00 6E 00 63 00 65 00 20 00 66 00 6F 00 72 00 20 00 50 00 6F 00 77 00 65 00 72 00 20 00 53 00 74 00 61 00 6E 00 63 00 65 00 20 00 74 00 6F 00 20 00 61 00 63 00 74 00 69 00 76 00 61 00 74 00 65 00 20 00 66 00 6F 00 72 00 20 00 23 00 74 00 69 00 6D 00 65 00 20 00 73 00 65 00 63 00 2E 00 20 00 23 00 64 00 61 00 6D 00 61 00 67 00 65 00 25 00 20 00 62 00 6F 00 6E 00 75 00 73 00 20 00 64 00 61 00 6D 00 61 00 67 00 65 00 20 00 64 00 65 00 61 00 6C 00 74 00 20 00 77 00 69 00 74 00 68 00 20 00 73 00 70 00 65 00 6C 00 6C 00 73 00 2E 00 20 00 31 00 30 00 30 00 25 00 20 00 62 00 6F 00 6E 00 75 00 73 00 20 00 64 00 65 00 61 00 6C 00 74 00 20 00 77 00 68 00 65 00 6E 00 20 00 45 00 71 00 75 00 69 00 6C 00 69 00 62 00 72 00 75 00 6D 00 20 00 69 00 73 00 20 00 75 00 73 00 65 00 64 00 2E 00 20 00 43 00 6F 00 6F 00 6C 00 64 00 6F 00 77 00 6E 00 73 00 20 00 6E 00 6F 00 74 00 20 00 61 00 70 00 70 00 6C 00 69 00 65 00 64 00 2E 00 20 00 30 00 20 00 4D 00 50 00 20 00 75 00 73 00 65 00 64 00 20 00 66 00 6F 00 72 00 20 00 44 00 61 00 72 00 6B 00 20 00 73 00 70 00 65 00 6C 00 6C 00 73 00 2E 00 20 00 23 00 78 00 25 00 20 00 48 00 50 00 20 00 72 00 65 00 73 00 74 00 00 00 00 00 00 00 00 8A 00 00 00 00 A0 99 B9 71 E3 D7 CE 01 01 01 10 00 32 30 31 33 2E 31 31 2E 30 31 7E 31 31 2E 33 30 78 05 00 00 01 00 00 C0 4E 95 D6 CE 01"));
       
        return mplew.getPacket();
    }

    public static byte[] warpCS1(MapleClient c, int types) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPENMORE.getValue());
        if (types == 2) {
            mplew.write(4);
            mplew.write(1);
            mplew.writeShort(1);

            mplew.write(HexTool.getByteArrayFromHexString("09 3D 00 10 30 3D 00 D4 B7 0F 00"));
            mplew.writeMapleAsciiString("http://www.redtube.com/cash1.jpg");
            mplew.write(HexTool.getByteArrayFromHexString("D9 FE FD 02 A0 A6 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 32 00 00 00 30 75 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 30 75 00 00 00 00 00 00 23 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 CE 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        } else {
            if (types == 1) {
                mplew.write(HexTool.getByteArrayFromHexString("03 01 5F 80 84 1E 00 08 00 46 61 76 6F 72 69 74 65 01 00 00 00 00 00 00 00 00 00 00 00 50 69 0F 00 12 00 53 70 65 63 69 61 6C 20 50 72 6F 6D 6F 74 69 6F 6E 73 01 00 00 00 02 00 00 00 00 00 00 00 B4 69 0F 00 0C 00 4E 65 77 20 41 72 72 69 76 61 6C 73 02 00 00 00 02 00 00 00 00 00 00 00 7C 6A 0F 00 15 00 4C 69 6D 69 74 65 64 20 54 69 6D 65 20 53 70 65 63 69 61 6C 73 02 00 00 00 00 00 00 00 01 00 00 00 E0 6A 0F 00 10 00 4C 69 6D 69 74 65 64 20 51 75 61 6E 74 69 74 79 02 00 00 00 00 00 00 00 00 00 00 00 90 6F 0F 00 09 00 48 61 6C 6C 6F 77 65 65 6E 02 00 00 00 01 00 00 00 00 00 00 00 60 90 0F 00 0B 00 54 69 6D 65 20 53 61 76 65 72 73 01 00 00 00 00 00 00 00 00 00 00 00 C4 90 0F 00 0E 00 54 65 6C 65 70 6F 72 74 20 52 6F 63 6B 73 02 00 00 00 00 00 00 00 00 00 00 00 28 91 0F 00 0B 00 49 74 65 6D 20 53 74 6F 72 65 73 02 00 00 00 00 00 00 00 01 00 00 00 8C 91 0F 00 0D 00 51 75 65 73 74 20 48 65 6C 70 65 72 73 02 00 00 00 00 00 00 00 00 00 00 00 54 92 0F 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 70 B7 0F 00 0E 00 52 61 6E 64 6F 6D 20 52 65 77 61 72 64 73 01 00 00 00 02 00 00 00 00 00 00 00 D4 B7 0F 00 10 00 47 61 63 68 61 70 6F 6E 20 54 69 63 6B 65 74 73 02 00 00 00 00 00 00 00 00 00 00 00 38 B8 0F 00 0E 00 53 75 72 70 72 69 73 65 20 42 6F 78 65 73 02 00 00 00 00 00 00 00 00 00 00 00 9C B8 0F 00 0D 00 53 70 65 63 69 61 6C 20 49 74 65 6D 73 02 00 00 00 00 00 00 00 00 00 00 00 80 DE 0F 00 17 00 45 71 75 69 70 6D 65 6E 74 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 01 00 00 00 00 00 00 00 00 00 00 00 E4 DE 0F 00 0D 00 4D 69 72 61 63 6C 65 20 43 75 62 65 73 02 00 00 00 00 00 00 00 01 00 00 00 10 E0 0F 00 0D 00 55 70 67 72 61 64 65 20 53 6C 6F 74 73 02 00 00 00 00 00 00 00 00 00 00 00 74 E0 0F 00 05 00 54 72 61 64 65 02 00 00 00 00 00 00 00 00 00 00 00 3C E1 0F 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 3D E1 0F 00 08 00 49 74 65 6D 20 54 61 67 03 00 00 00 00 00 00 00 00 00 00 00 3E E1 0F 00 0B 00 49 74 65 6D 20 47 75 61 72 64 73 03 00 00 00 00 00 00 00 00 00 00 00 A0 E1 0F 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 90 05 10 00 17 00 43 68 61 72 61 63 74 65 72 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 01 00 00 00 00 00 00 00 00 00 00 00 F4 05 10 00 13 00 53 50 2F 41 50 20 6D 6F 64 69 66 69 63 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 58 06 10 00 0B 00 45 58 50 20 43 6F 75 70 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 BC 06 10 00 0C 00 44 72 6F 70 20 43 6F 75 70 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 20 07 10 00 0F 00 49 6E 76 65 6E 74 6F 72 79 20 73 6C 6F 74 73 02 00 00 00 00 00 00 00 00 00 00 00 84 07 10 00 13 00 53 6B 69 6C 6C 20 4D 6F 64 69 66 69 63 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 E8 07 10 00 0A 00 50 72 6F 74 65 63 74 69 6F 6E 02 00 00 00 00 00 00 00 00 00 00 00 4C 08 10 00 07 00 57 65 64 64 69 6E 67 02 00 00 00 00 00 00 00 00 00 00 00 B0 08 10 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 14 09 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 A0 2C 10 00 09 00 45 71 75 69 70 6D 65 6E 74 01 00 00 00 00 00 00 00 00 00 00 00 04 2D 10 00 06 00 57 65 61 70 6F 6E 02 00 00 00 00 00 00 00 00 00 00 00 68 2D 10 00 03 00 48 61 74 02 00 00 00 00 00 00 00 00 00 00 00 69 2D 10 00 0F 00 46 75 6C 6C 20 48 65 61 64 20 43 6F 76 65 72 03 00 00 00 00 00 00 00 00 00 00 00 6A 2D 10 00 07 00 42 65 61 6E 69 65 73 03 00 00 00 00 00 00 00 00 00 00 00 6B 2D 10 00 07 00 48 61 69 72 70 69 6E 03 00 00 00 00 00 00 00 00 00 00 00 6C 2D 10 00 08 00 48 61 69 72 62 61 6E 64 03 00 00 00 00 00 00 00 00 00 00 00 6D 2D 10 00 0D 00 46 75 6C 6C 20 42 72 69 6D 20 48 61 74 03 00 00 00 00 00 00 00 00 00 00 00 6E 2D 10 00 04 00 43 61 70 73 03 00 00 00 00 00 00 00 00 00 00 00 73 2D 10 00 05 00 4F 74 68 65 72 03 00 00 00 00 00 00 00 00 00 00 00 CC 2D 10 00 04 00 46 61 63 65 02 00 00 00 00 00 00 00 00 00 00 00 30 2E 10 00 03 00 45 79 65 02 00 00 00 00 00 00 00 00 00 00 00 94 2E 10 00 09 00 41 63 63 65 73 73 6F 72 79 02 00 00 00 00 00 00 00 00 00 00 00 95 2E 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 F8 2E 10 00 08 00 45 61 72 72 69 6E 67 73 02 00 00 00 00 00 00 00 00 00 00 00 5C 2F 10 00 07 00 4F 76 65 72 61 6C 6C 02 00 00 00 00 00 00 00 00 00 00 00 C0 2F 10 00 03 00 54 6F 70 02 00 00 00 00 00 00 00 00 00 00 00 C1 2F 10 00 0C 00 4C 6F 6E 67 20 53 6C 65 65 76 65 73 03 00 00 00 00 00 00 00 00 00 00 00 C2 2F 10 00 0D 00 53 68 6F 72 74 20 53 6C 65 65 76 65 73 03 00 00 00 00 00 00 00 00 00 00 00 24 30 10 00 06 00 42 6F 74 74 6F 6D 02 00 00 00 00 00 00 00 00 00 00 00 25 30 10 00 06 00 53 68 6F 72 74 73 03 00 00 00 00 00 00 00 00 00 00 00 26 30 10 00 05 00 50 61 6E 74 73 03 00 00 00 00 00 00 00 00 00 00 00 27 30 10 00 06 00 53 6B 69 72 74 73 03 00 00 00 00 00 00 00 00 00 00 00 88 30 10 00 05 00 53 68 6F 65 73 02 00 00 00 00 00 00 00 00 00 00 00 EC 30 10 00 05 00 47 6C 6F 76 65 02 00 00 00 00 00 00 00 00 00 00 00 50 31 10 00 04 00 52 69 6E 67 02 00 00 00 00 00 00 00 00 00 00 00 51 31 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 52 31 10 00 0A 00 46 72 69 65 6E 64 73 68 69 70 03 00 00 00 00 00 00 00 00 00 00 00 53 31 10 00 05 00 4C 61 62 65 6C 03 00 00 00 00 00 00 00 00 00 00 00 54 31 10 00 05 00 51 75 6F 74 65 03 00 00 00 00 00 00 00 00 00 00 00 56 31 10 00 04 00 53 6F 6C 6F 03 00 00 00 00 00 00 00 00 00 00 00 B4 31 10 00 04 00 43 61 70 65 02 00 00 00 00 00 00 00 00 00 00 00 7C 32 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 01 00 00 00 E0 32 10 00 0B 00 54 72 61 6E 73 70 61 72 65 6E 74 02 00 00 00 00 00 00 00 00 00 00 00 B0 53 10 00 0A 00 41 70 70 65 61 72 61 6E 63 65 01 00 00 00 00 00 00 00 00 00 00 00 14 54 10 00 0D 00 42 65 61 75 74 79 20 50 61 72 6C 6F 72 02 00 00 00 00 00 00 00 00 00 00 00 15 54 10 00 04 00 48 61 69 72 03 00 00 00 00 00 00 00 00 00 00 00 16 54 10 00 04 00 46 61 63 65 03 00 00 00 00 00 00 00 00 00 00 00 17 54 10 00 04 00 53 6B 69 6E 03 00 00 00 00 00 00 00 00 00 00 00 78 54 10 00 12 00 46 61 63 69 61 6C 20 45 78 70 72 65 73 73 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 DC 54 10 00 06 00 45 66 66 65 63 74 02 00 00 00 00 00 00 00 00 00 00 00 40 55 10 00 0F 00 54 72 61 6E 73 66 6F 72 6D 61 74 69 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 A4 55 10 00 07 00 53 70 65 63 69 61 6C 02 00 00 00 00 00 00 00 00 00 00 00 C0 7A 10 00 03 00 50 65 74 01 00 00 00 00 00 00 00 00 00 00 00 24 7B 10 00 04 00 50 65 74 73 02 00 00 00 00 00 00 00 00 00 00 00 88 7B 10 00 0E 00 50 65 74 20 41 70 70 65 61 72 61 6E 63 65 02 00 00 00 00 00 00 00 00 00 00 00 EC 7B 10 00 07 00 50 65 74 20 55 73 65 02 00 00 00 00 00 00 00 00 00 00 00 50 7C 10 00 08 00 50 65 74 20 46 6F 6F 64 02 00 00 00 00 00 00 00 00 00 00 00 B4 7C 10 00 08 00 50 61 63 6B 61 67 65 73 02 00 00 00 00 00 00 00 00 00 00 00 D0 A1 10 00 0B 00 46 72 65 65 20 4D 61 72 6B 65 74 01 00 00 00 00 00 00 00 00 00 00 00 34 A2 10 00 0C 00 53 68 6F 70 20 50 65 72 6D 69 74 73 02 00 00 00 00 00 00 00 00 00 00 00 98 A2 10 00 05 00 4F 74 68 65 72 02 00 00 00 00 00 00 00 00 00 00 00 E0 C8 10 00 14 00 4D 65 73 73 65 6E 67 65 72 20 61 6E 64 20 53 6F 63 69 61 6C 01 00 00 00 00 00 00 00 00 00 00 00 44 C9 10 00 0A 00 4D 65 67 61 70 68 6F 6E 65 73 02 00 00 00 00 00 00 00 00 00 00 00 A8 C9 10 00 0A 00 4D 65 73 73 65 6E 67 65 72 73 02 00 00 00 00 00 00 00 00 00 00 00 0C CA 10 00 15 00 47 75 69 6C 64 20 46 6F 72 75 6D 20 45 6D 6F 74 69 63 6F 6E 73 02 00 00 00 00 00 00 00 00 00 00 00 70 CA 10 00 0F 00 57 65 61 74 68 65 72 20 45 66 66 65 63 74 73 02 00 00 00 00 00 00 00 00 00 00 00 71 CA 10 00 05 00 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 72 CA 10 00 09 00 4E 6F 6E 2D 53 74 61 74 73 03 00 00 00 00 00 00 00 00 00 00 00 20 D6 13 00 0C 00 4D 6F 6E 73 74 65 72 20 4C 69 66 65 01 00 00 00 00 00 00 00 00 00 00 00 84 D6 13 00 0A 00 49 6E 63 75 62 61 74 6F 72 73 02 00 00 00 00 00 00 00 00 00 00 00 E8 D6 13 00 04 00 47 65 6D 73 02 00 00 00 00 00 00 00 00 00 00 00"));
            } else if (types == 2) {
                mplew.write(HexTool.getByteArrayFromHexString("04 01 0A 00 09 3D 00 20 57 3D 00 E4 DE 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 34 64 32 30 61 36 31 61 2D 36 30 63 61 2D 34 37 32 63 2D 39 36 61 33 2D 34 35 36 36 35 64 64 31 36 32 61 33 2E 6A 70 67 1B E3 F5 05 CA 3D 4D 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 0D 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AC 0D 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 20 57 3D 00 E4 DE 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 34 64 32 30 61 36 31 61 2D 36 30 63 61 2D 34 37 32 63 2D 39 36 61 33 2D 34 35 36 36 35 64 64 31 36 32 61 33 2E 6A 70 67 1C E3 F5 05 CA 3D 4D 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 B8 88 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 B8 88 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 12 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 40 A5 3D 00 38 B8 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 D6 A4 98 00 B0 AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 48 0D 00 00 00 40 AB 82 7A B1 CD 01 00 80 05 BB 46 E6 17 02 00 40 AB 82 7A B1 CD 01 00 80 05 BB 46 E6 17 02 48 0D 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 01 01 00 01 02 00 00 00 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 40 A5 3D 00 38 B8 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 D7 A4 98 00 B0 AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D0 84 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 D0 84 00 00 00 00 00 00 0B 00 00 00 1E 00 00 00 01 01 01 00 01 02 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 40 A5 3D 00 B4 7C 10 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 7C E3 F5 05 8A E6 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 34 3A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 40 08 D1 82 CF CE 01 00 C0 D0 22 83 DA CE 01 C4 22 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 7D E3 F5 05 40 4C 4C 00 01 00 00 00 00 19 00 00 00 19 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 7E E3 F5 05 95 80 1B 00 01 00 00 00 C4 09 00 00 C4 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 7F E3 F5 05 0A F5 4F 00 01 00 00 00 70 17 00 00 00 00 00 00 00 00 00 00 06 00 00 00 5A 00 00 00 02 00 00 00 17 E1 F5 05 C4 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 18 E1 F5 05 C5 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 00 09 3D 00 40 A5 3D 00 B4 7C 10 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 80 E3 F5 05 8C E6 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 34 3A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 40 08 D1 82 CF CE 01 00 C0 D0 22 83 DA CE 01 C4 22 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 81 E3 F5 05 42 4C 4C 00 01 00 00 00 00 19 00 00 00 19 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 05 E4 F5 05 97 80 1B 00 01 00 00 00 C4 09 00 00 C4 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 7F E3 F5 05 0A F5 4F 00 01 00 00 00 70 17 00 00 70 17 00 00 00 00 00 00 06 00 00 00 5A 00 00 00 02 00 00 00 17 E1 F5 05 C4 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 18 E1 F5 05 C5 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 00 09 3D 00 40 A5 3D 00 B4 7C 10 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 82 E3 F5 05 8B E6 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 34 3A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 40 08 D1 82 CF CE 01 00 C0 D0 22 83 DA CE 01 C4 22 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 83 E3 F5 05 41 4C 4C 00 01 00 00 00 00 19 00 00 00 19 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 04 E4 F5 05 96 80 1B 00 01 00 00 00 C4 09 00 00 C4 09 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 7F E3 F5 05 0A F5 4F 00 01 00 00 00 70 17 00 00 00 00 00 00 00 00 00 00 06 00 00 00 5A 00 00 00 02 00 00 00 17 E1 F5 05 C4 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 18 E1 F5 05 C5 61 54 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 00 09 3D 00 40 A5 3D 00 B4 7C 10 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 38 66 38 32 34 62 64 39 2D 37 31 36 33 2D 34 31 65 32 2D 62 65 39 39 2D 61 31 61 37 38 33 30 64 35 31 35 61 2E 6A 70 67 D0 E3 F5 05 13 E8 8A 00 01 00 00 00 03 00 00 00 01 00 00 00 00 00 00 00 34 6C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 40 08 D1 82 CF CE 01 00 C0 D0 22 83 DA CE 01 58 2F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 7D E3 F5 05 40 4C 4C 00 01 00 00 00 00 19 00 00 00 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 81 E3 F5 05 42 4C 4C 00 01 00 00 00 00 19 00 00 00 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 83 E3 F5 05 41 4C 4C 00 01 00 00 00 00 19 00 00 00 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 7F E3 F5 05 0A F5 4F 00 01 00 00 00 70 17 00 00 58 02 00 00 00 00 00 00 06 00 00 00 5A 00 00 00 02 00 00 00 65 94 96 03 20 50 53 00 01 00 00 00 C4 09 00 00 00 00 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 02 00 00 00 00 09 3D 00 30 7E 3D 00 B4 69 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 61 39 36 36 66 33 34 66 2D 37 38 36 39 2D 34 39 66 33 2D 62 39 34 33 2D 33 62 30 33 66 32 33 39 64 35 32 65 2E 6A 70 67 10 A2 98 00 8B AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 14 00 00 00 08 07 00 00 00 80 96 B6 5F 8C CC 01 00 80 05 BB 46 E6 17 02 00 80 96 B6 5F 8C CC 01 00 80 05 BB 46 E6 17 02 08 07 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 3D 00 30 7E 3D 00 B4 69 0F 00 55 00 68 74 74 70 3A 2F 2F 6E 78 63 61 63 68 65 2E 6E 65 78 6F 6E 2E 6E 65 74 2F 73 70 6F 74 6C 69 67 68 74 2F 32 38 36 2F 30 30 45 53 33 2D 61 39 36 36 66 33 34 66 2D 37 38 36 39 2D 34 39 66 33 2D 62 39 34 33 2D 33 62 30 33 66 32 33 39 64 35 32 65 2E 6A 70 67 B0 A4 98 00 8B AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 14 00 00 00 50 46 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 50 46 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            } else if (types == 3) {
                mplew.write(HexTool.getByteArrayFromHexString("05 01 04 C0 C6 2D 00 D0 ED 2D 00 E4 DE 0F 00 00 00 DE FE FD 02 72 3D 4D 00 01 00 00 00 00 00 00 00 00 00 00 00 27 00 00 00 38 4A 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 38 4A 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 A9 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 E4 DE 0F 00 00 00 D4 FE FD 02 70 3D 4D 00 01 00 00 00 00 00 00 00 00 00 00 00 27 00 00 00 E0 2E 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 E0 2E 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 C1 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 D4 B7 0F 00 00 00 DA FE FD 02 A0 A6 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 32 00 00 00 10 27 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 10 27 00 00 00 00 00 00 0B 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 1D 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 D0 ED 2D 00 38 B8 0F 00 00 00 87 2C 9A 00 AC AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 22 00 00 00 48 0D 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 48 0D 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 01 01 00 01 02 00 00 00 E0 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            } else if (types == 4) {
                mplew.write(HexTool.getByteArrayFromHexString("06 01 05 C0 C6 2D 00 E0 14 2E 00 15 54 10 00 00 00 9C F1 FA 02 58 95 4E 00 01 00 00 00 04 00 00 00 00 00 00 00 32 00 00 00 E4 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 40 01 00 00 5A 00 00 00 01 00 00 00 5A 00 00 00 00 01 01 00 00 02 00 00 00 18 07 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 16 54 10 00 00 00 C9 F1 FA 02 35 9D 4E 00 01 00 00 00 04 00 00 00 00 00 00 00 32 00 00 00 E4 0C 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 40 01 00 00 5A 00 00 00 01 00 00 00 5A 00 00 00 00 01 01 00 00 02 00 00 00 70 04 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 C4 90 0F 00 00 00 BF C3 C9 01 84 E7 4C 00 01 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 AC 26 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AC 26 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 01 01 00 01 02 00 00 00 22 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 38 B8 0F 00 00 00 87 2C 9A 00 AC AE 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 22 00 00 00 48 0D 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 4A 01 00 00 5A 00 00 00 01 00 00 00 1E 00 00 00 00 01 01 00 00 02 00 00 00 10 08 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 E0 14 2E 00 D4 B7 0F 00 00 00 D9 FE FD 02 A0 A6 4F 00 01 00 00 00 00 00 00 00 00 00 00 00 32 00 00 00 30 75 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AE 0B 00 00 5A 00 00 00 23 00 00 00 5A 00 00 00 00 01 01 00 00 02 00 00 00 26 03 00 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            } else if (types == 5) {
                mplew.write(HexTool.getByteArrayFromHexString("09 01 01 C0 C6 2D 00 00 63 2E 00 B0 08 10 00 00 00 18 E3 F5 05 A8 69 52 00 01 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 01 00 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 01 01 01 01 02 00 00 00 65 01 00 00 32 00 00 00 0A 00 31 4D 53 35 34 30 31 30 30 30 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            } else if (types == 6) {
                mplew.write(HexTool.getByteArrayFromHexString("08 01 05 C0 C6 2D 00 F0 3B 2E 00 C4 90 0F 00 00 00 BF C3 C9 01 84 E7 4C 00 01 00 00 00 04 00 00 00 00 00 00 00 0F 00 00 00 AC 26 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 AC 26 00 00 00 00 00 00 01 00 00 00 1E 00 00 00 01 01 01 00 01 02 00 00 00 1F 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 74 E0 0F 00 00 00 7C FE FD 02 81 3A 54 00 01 00 00 00 04 00 00 00 00 00 00 00 02 00 00 00 A0 0F 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 A0 0F 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 B5 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 E8 07 10 00 00 00 40 FE FD 02 70 13 54 00 01 00 00 00 04 00 00 00 00 00 00 00 01 00 00 00 F4 01 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 F4 01 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 8D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 10 E0 0F 00 00 00 3D FE FD 02 D0 FD 54 00 01 00 00 00 04 00 00 00 00 00 00 00 04 00 00 00 24 13 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 24 13 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 77 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C0 C6 2D 00 F0 3B 2E 00 74 E0 0F 00 00 00 35 FE FD 02 80 3A 54 00 01 00 00 00 04 00 00 00 00 00 00 00 03 00 00 00 B8 0B 00 00 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 00 80 22 D6 94 EF C4 01 00 80 05 BB 46 E6 17 02 B8 0B 00 00 00 00 00 00 01 00 00 00 5A 00 00 00 01 01 01 00 01 02 00 00 00 F1 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            }
        }
        return mplew.getPacket();
    }

    public static void addModCashItemInfo(MaplePacketLittleEndianWriter mplew, CashItemInfo.CashModInfo item) {
        int flags = item.flags;
        mplew.writeInt(item.sn);
        mplew.writeInt(flags);
        if ((flags & 0x1) != 0) {
            mplew.writeInt(item.itemid);
        }
        if ((flags & 0x2) != 0) {
            mplew.writeShort(item.count);
        }
        if ((flags & 0x10) != 0) {
            mplew.write(item.priority);
        }
        if ((flags & 0x4) != 0) {
            mplew.writeInt(item.discountPrice);
        }
        if ((flags & 0x8) != 0) {
            mplew.write(item.unk_1 - 1);
        }
        if ((flags & 0x20) != 0) {
            mplew.writeShort(item.period);
        }
        if ((flags & 0x20000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x40000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x40) != 0) {
            mplew.writeInt(0);
        }
        if ((flags & 0x80) != 0) {
            mplew.writeInt(item.meso);
        }
        if ((flags & 0x100) != 0) {
            mplew.write(item.unk_2 - 1);
        }
        if ((flags & 0x200) != 0) {
            mplew.write(item.gender);
        }
        if ((flags & 0x400) != 0) {
            mplew.write(item.showUp ? 1 : 0);
        }
        if ((flags & 0x800) != 0) {
            mplew.write(item.mark);
        }
        if ((flags & 0x1000) != 0) {
            mplew.write(item.unk_3 - 1);
        }
        if ((flags & 0x2000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x4000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x8000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x10000) != 0) {
            List pack = CashItemFactory.getInstance().getPackageItems(item.sn);
            if (pack == null) {
                mplew.write(0);
            } else {
                mplew.write(pack.size());
                for (int i = 0; i < pack.size(); i++) {
                    mplew.writeInt(((Integer) pack.get(i)).intValue());
                }
            }
        }
        if (((flags & 0x80000) == 0) || (((flags & 0x100000) == 0)
                || ((flags & 0x200000) != 0))) {
            mplew.write(0);
        }
    }

    public static byte[] chargeCash() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_CHARGE_CASH.getValue());
        mplew.writeMapleAsciiString("http://www.google.com");
        mplew.writeMapleAsciiString("http://www.google.com");

        return mplew.getPacket();
    }

    public static byte[] showNXMapleTokens(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(chr.getCSPoints(1));
        mplew.writeInt(chr.getCSPoints(2));
        mplew.writeInt(chr.getCSPoints(4));

        return mplew.getPacket();
    }

    public static byte[] LimitGoodsCountChanged() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] getCSInventory(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(Operation_Code + 3);
        CashShop mci = c.getPlayer().getCashInventory();
        mplew.writeShort(mci.getItemsSize());
        if (mci.getItemsSize() > 0) {
            int size = 0;
            for (Item itemz : mci.getInventory()) {
                addCashItemInfo(mplew, itemz, c.getAccID(), 0);
                if ((GameConstants.isPet(itemz.getItemId())) || (GameConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP)) {
                    size++;
                }
            }
            mplew.writeInt(size);
            for (Item itemz : mci.getInventory()) {
                if ((GameConstants.isPet(itemz.getItemId())) || (GameConstants.getInventoryType(itemz.getItemId()) == MapleInventoryType.EQUIP)) {
                    PacketHelper.addItemInfo(mplew, itemz);
                }
            }
        }
        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeInt(c.getCharacterSlots());
        mplew.writeShort(1);

        return mplew.getPacket();
    }

    public static byte[] getCSGifts(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x6E);
        List<Pair<Item, String>> mci = c.getPlayer().getCashInventory().loadGifts();
        mplew.writeShort(mci.size());
        for (Pair<Item, String> mcz : mci) {
            mplew.writeLong(((Item) mcz.getLeft()).getUniqueId());
            mplew.writeInt(((Item) mcz.getLeft()).getItemId());
            mplew.writeAsciiString(((Item) mcz.getLeft()).getGiftFrom(), 13);
            mplew.writeAsciiString((String) mcz.getRight(), 73);
        }

        return mplew.getPacket();
    }

    public static byte[] sendWishList(MapleCharacter chr, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (update ? 16 : 9));
        int[] list = chr.getWishlist();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(list[i] != -1 ? list[i] : 0);
        }

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(Item item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        //   mplew.write(HexTool.hex("77 74 50 22 01 00 00 00 00 18 BD 63 02 00 00 00 00 CA 3D 4D 00 1B E3 F5 05 01 00 00 0E F5 71 6D 77 14 FE E4 0E 00 00 00 10 81 8A C6 0F 1F CF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));


        mplew.write(0x77);
        addCashItemInfo(mplew, item, accid, sn);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItem(int itemid, int sn, int uniqueid, int accid, int quantity, String giftFrom, long expire) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        //     mplew.write(HexTool.hex("77 74 50 22 01 00 00 00 00 18 BD 63 02 00 00 00 00 CA 3D 4D 00 1B E3 F5 05 01 00 00 0E F5 71 6D 77 14 FE E4 0E 00 00 00 10 81 8A C6 0F 1F CF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
        mplew.write(0x77);
        addCashItemInfo(mplew, uniqueid, accid, itemid, sn, quantity, giftFrom, expire);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }

    public static byte[] showBoughtCSItemFailed(int mode, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 18);
        mplew.write(mode);
        if ((mode == 29) || (mode == 30)) {
            mplew.writeInt(sn);
        } else if (mode == 69) {
            mplew.write(1);
        } else if (mode == 85) {
            mplew.writeInt(sn);
            mplew.writeLong(System.currentTimeMillis());
        }

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSPackage(Map<Integer, Item> ccc, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 75); // 72 = Similar structure to showBoughtCSItemFailed
        mplew.write(ccc.size());
        for (Entry<Integer, Item> sn : ccc.entrySet()) {
            addCashItemInfo(mplew, sn.getValue(), accid, sn.getKey().intValue());
        }
        mplew.writeShort(0); // Purchase Maple Points = 1, Item = 0
        mplew.writeInt(0); // SN
        //mplew.writeLong(System.currentTimeMillis());

        return mplew.getPacket();
    }

    public static byte[] sendGift(int price, int itemid, int quantity, String receiver, boolean packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (packages ? 78 : 29));
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(itemid);
        mplew.writeShort(quantity);
        if (packages) {
            mplew.writeShort(0);
        }
        mplew.writeInt(price);

        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(Map<Integer, Item> items, int mesos, int maplePoints, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 19);
        mplew.write(items.size());
        for (Map.Entry item : items.entrySet()) {
            addCashItemInfo(mplew, (Item) item.getValue(), c.getAccID(), ((Integer) item.getKey()).intValue());
        }
        mplew.writeInt(maplePoints);
        mplew.writeInt(0);

        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static byte[] showCouponGifted(Map<Integer, Item> items, String receiver, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 21);
        mplew.writeMapleAsciiString(receiver);
        mplew.write(items.size());
        for (Map.Entry item : items.entrySet()) {
            addCashItemInfo(mplew, (Item) item.getValue(), c.getAccID(), ((Integer) item.getKey()).intValue());
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] increasedInvSlots(int inv, int slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 26);
        mplew.write(inv);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] increasedStorageSlots(int slots, boolean characterSlots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (characterSlots ? 30 : 28));
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] increasedPendantSlots() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 34);
        mplew.writeShort(0);
        mplew.writeShort(100);

        return mplew.getPacket();
    }

    public static byte[] confirmFromCSInventory(Item item, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 39);
        mplew.writeShort(pos);
        PacketHelper.addItemInfo(mplew, item);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] confirmToCSInventory(Item item, int accId, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 41);
        addCashItemInfo(mplew, item, accId, sn, false);

        return mplew.getPacket();
    }

    public static byte[] cashItemDelete(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 40);
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    public static byte[] rebateCashItem() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 67);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendBoughtRings(boolean couple, Item item, int sn, int accid, String receiver) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + (couple ? 69 : 79));
        addCashItemInfo(mplew, item, accid, sn);
        mplew.writeMapleAsciiString(receiver);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(1);

        return mplew.getPacket();
    }

    public static byte[] receiveFreeCSItem(Item item, int sn, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 87);
        addCashItemInfo(mplew, item, accid, sn);

        return mplew.getPacket();
    }

    public static byte[] cashItemExpired(int uniqueid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 42);
        mplew.writeLong(uniqueid);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCSQuestItem(int price, short quantity, byte position, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 75);
        mplew.writeInt(1);
        mplew.writeInt(quantity);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] updatePurchaseRecord() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 94);
        mplew.writeInt(0);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] sendCashRefund(int cash) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 97);
        mplew.writeInt(0);
        mplew.writeInt(cash);

        return mplew.getPacket();
    }

    public static byte[] sendRandomBox(int uniqueid, Item item, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 99);
        mplew.writeLong(uniqueid);
        mplew.writeInt(1302000);
        PacketHelper.addItemInfo(mplew, item);
        mplew.writeShort(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] sendCashGachapon(boolean cashItem, int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 109);
        mplew.writeLong(idFirst);
        mplew.writeInt(0);
        mplew.write(cashItem ? 1 : 0);
        if (cashItem) {
            addCashItemInfo(mplew, item, accid, 0);
        }
        mplew.writeInt(item.getItemId());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] sendTwinDragonEgg(boolean test1, boolean test2, int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 111);
        mplew.write(test1 ? 1 : 0);
        mplew.write(test2 ? 1 : 0);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(4);
        if ((test1) && (test2)) {
            addCashItemInfo(mplew, item, accid, 0);
        }

        return mplew.getPacket();
    }

    public static byte[] sendBoughtMaplePoints(int maplePoints) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 113);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(maplePoints);

        return mplew.getPacket();
    }

    public static byte[] changeNameCheck(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANGE_NAME_CHECK.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] changeNameResponse(int mode, int pic) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANGE_NAME_RESPONSE.getValue());
        mplew.writeInt(0);
        mplew.write(mode);
        mplew.writeInt(pic);

        return mplew.getPacket();
    }

    public static byte[] receiveGachaStamps(boolean invfull, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GACHAPON_STAMPS.getValue());
        mplew.write(invfull ? 0 : 1);
        if (!invfull) {
            mplew.writeInt(amount);
        }

        return mplew.getPacket();
    }

    public static byte[] freeCashItem(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FREE_CASH_ITEM.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] showXmasSurprise(boolean full, int idFirst, Item item, int accid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.XMAS_SURPRISE.getValue());
        mplew.write(full ? 212 : 213);
        if (!full) {
            mplew.writeLong(idFirst);
            mplew.writeInt(0);
            addCashItemInfo(mplew, item, accid, 0);
            mplew.writeInt(item.getItemId());
            mplew.write(1);
            mplew.write(1);
        }

        return mplew.getPacket();
    }

    public static byte[] showOneADayInfo(boolean show, int sn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ONE_A_DAY.getValue());
        mplew.writeInt(100);
        mplew.writeInt(100000);
        mplew.writeInt(1);
        mplew.writeInt(20121231);
        mplew.writeInt(sn);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] playCashSong(int itemid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CASH_SONG.getValue());
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    public static byte[] useAlienSocket(boolean start) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ALIEN_SOCKET_CREATOR.getValue());
        mplew.write(start ? 0 : 2);

        return mplew.getPacket();
    }

    public static byte[] ViciousHammer(boolean start, int hammered) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(start ? 63 : 67);
        mplew.writeInt(0);
        if (start) {
            mplew.writeInt(hammered);
        }

        return mplew.getPacket();
    }

    public static byte[] changePetFlag(int uniqueId, boolean added, int flagAdded) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_FLAG_CHANGE.getValue());

        mplew.writeLong(uniqueId);
        mplew.write(added ? 1 : 0);
        mplew.writeShort(flagAdded);

        return mplew.getPacket();
    }

    public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());

        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(slot);

        return mplew.getPacket();
    }

    public static byte[] OnMemoResult(byte act, byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(act);
        if (act == 5) {
            mplew.write(mode);
        }

        return mplew.getPacket();
    }

    public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(PacketHelper.getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(notes.getInt("gift"));
            notes.next();
        }

        return mplew.getPacket();
    }

    public static byte[] useChalkboard(int charid, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());

        mplew.writeInt(charid);
        if ((msg == null) || (msg.length() <= 0)) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(msg);
        }

        return mplew.getPacket();
    }

    public static byte[] OnMapTransferResult(MapleCharacter chr, byte vip, boolean delete) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(delete ? 2 : 3);
        mplew.write(vip);
        if (vip == 1) {
            int[] map = chr.getRegRocks();
            for (int i = 0; i < 5; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 2) {
            int[] map = chr.getRocks();
            for (int i = 0; i < 10; i++) {
                mplew.writeInt(map[i]);
            }
        } else if (vip == 3) {
            int[] map = chr.getHyperRocks();
            for (int i = 0; i < 13; i++) {
                mplew.writeInt(map[i]);
            }
        }

        return mplew.getPacket();
    }

    public static final void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn) {
        addCashItemInfo(mplew, item, accId, sn, true);
    }

    public static final void addCashItemInfo(MaplePacketLittleEndianWriter mplew, Item item, int accId, int sn, boolean isFirst) {
        addCashItemInfo(mplew, item.getUniqueId(), accId, item.getItemId(), sn, item.getQuantity(), item.getGiftFrom(), item.getExpiration(), isFirst);
    }

    public static final void addCashItemInfo(MaplePacketLittleEndianWriter mplew, int uniqueid, int accId, int itemid, int sn, int quantity, String sender, long expire) {
        addCashItemInfo(mplew, uniqueid, accId, itemid, sn, quantity, sender, expire, true);
    }

    public static final void addCashItemInfo(MaplePacketLittleEndianWriter mplew, int uniqueid, int accId, int itemid, int sn, int quantity, String sender, long expire, boolean isFirst) {
        mplew.writeLong(uniqueid > 0 ? uniqueid : 0L);
        mplew.writeLong(accId);
        mplew.writeInt(itemid);
        mplew.writeInt(isFirst ? sn : 0);
        mplew.writeShort(quantity);
        mplew.writeAsciiString(sender, 13);
        PacketHelper.addExpirationTime(mplew, expire);
        mplew.writeLong(isFirst ? 0L : sn);
        mplew.writeZeroBytes(10);
    }

    public static byte[] sendCSFail(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(Operation_Code + 22);
        mplew.write(err);

        return mplew.getPacket();
    }

    public static byte[] enableCSUse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_USE.getValue());
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] enableCSUse1() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CS_USE1.getValue());
        mplew.writeInt(3);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPack(int f1, int f2, int f3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(215);
        mplew.writeInt(f1);
        mplew.writeInt(f2);
        mplew.writeInt(f3);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackClick() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(213);

        return mplew.getPacket();
    }

    public static byte[] getBoosterPackReveal() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_PACK.getValue());
        mplew.write(214);

        return mplew.getPacket();
    }

    public static byte[] sendMesobagFailed(boolean random) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(random ? SendPacketOpcode.RANDOM_MESOBAG_FAILURE.getValue() : SendPacketOpcode.MESOBAG_FAILURE.getValue());

        return mplew.getPacket();
    }

    public static byte[] sendMesobagSuccess(int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESOBAG_SUCCESS.getValue());
        mplew.writeInt(mesos);
        return mplew.getPacket();
    }

    public static byte[] sendRandomMesobagSuccess(int size, int mesos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RANDOM_MESOBAG_SUCCESS.getValue());
        mplew.write(size);
        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static final byte[] startMTS(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPEN.getValue());

        PacketHelper.addCharacterInfo(mplew, chr);
        if (!GameConstants.GMS) {
            mplew.writeMapleAsciiString("T13333333337W");
        }
        mplew.writeInt(10000);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(24);
        mplew.writeInt(168);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    public static final byte[] sendMTS(List<MTSStorage.MTSItemInfo> items, int tab, int type, int page, int pages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(21);
        mplew.writeInt(pages);
        mplew.writeInt(items.size());
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);

        for (MTSStorage.MTSItemInfo item : items) {
            addMTSItemInfo(mplew, item);
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static final byte[] showMTSCash(MapleCharacter p) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GET_MTS_TOKENS.getValue());
        mplew.writeInt(p.getCSPoints(1));
        mplew.writeInt(p.getCSPoints(2));
        return mplew.getPacket();
    }

    public static final byte[] getMTSWantedListingOver(int nx, int items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(61);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static final byte[] getMTSConfirmSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(29);
        return mplew.getPacket();
    }

    public static final byte[] getMTSFailSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(30);
        mplew.write(66);
        return mplew.getPacket();
    }

    public static final byte[] getMTSConfirmBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(51);
        return mplew.getPacket();
    }

    public static final byte[] getMTSFailBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(52);
        mplew.write(66);
        return mplew.getPacket();
    }

    public static final byte[] getMTSConfirmCancel() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(37);
        return mplew.getPacket();
    }

    public static final byte[] getMTSFailCancel() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(38);
        mplew.write(66);
        return mplew.getPacket();
    }

    public static final byte[] getMTSConfirmTransfer(int quantity, int pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(39);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    private static final void addMTSItemInfo(MaplePacketLittleEndianWriter mplew, MTSStorage.MTSItemInfo item) {
        PacketHelper.addItemInfo(mplew, item.getItem());
        mplew.writeInt(item.getId());
        mplew.writeInt(item.getTaxes());
        mplew.writeInt(item.getPrice());
        mplew.writeZeroBytes(GameConstants.GMS ? 4 : 8);
        mplew.writeLong(PacketHelper.getTime(item.getEndingDate()));
        mplew.writeMapleAsciiString(item.getSeller());
        mplew.writeMapleAsciiString(item.getSeller());
        mplew.writeZeroBytes(28);
    }

    public static final byte[] getNotYetSoldInv(List<MTSStorage.MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(35);

        mplew.writeInt(items.size());

        for (MTSStorage.MTSItemInfo item : items) {
            addMTSItemInfo(mplew, item);
        }

        return mplew.getPacket();
    }

    public static final byte[] getTransferInventory(List<Item> items, boolean changed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(33);

        mplew.writeInt(items.size());
        int i = 0;
        for (Item item : items) {
            PacketHelper.addItemInfo(mplew, item);
            mplew.writeInt(2147483647 - i);
            mplew.writeZeroBytes(GameConstants.GMS ? 52 : 56);
            i++;
        }
        mplew.writeInt(-47 + i - 1);
        mplew.write(changed ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] addToCartMessage(boolean fail, boolean remove) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        if (remove) {
            if (fail) {
                mplew.write(44);
                mplew.writeInt(-1);
            } else {
                mplew.write(43);
            }
        } else if (fail) {
            mplew.write(42);
            mplew.writeInt(-1);
        } else {
            mplew.write(41);
        }

        return mplew.getPacket();
    }

    public static byte[] cash_send_item(int types, List<cashinformation> dataCache) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPENMORE_ITEM.getValue());
        mplew.write(11);
        if (dataCache.isEmpty()) {
            mplew.write(0);
            mplew.write(3);
        } else {
            mplew.write(1);
            mplew.write(dataCache.size());
            for (cashinformation entry : dataCache) {
                mplew.write(HexTool.hex("40 42 0F 00 A0 E1 0F 00 A0 E1 0F 00 00 00"));
                mplew.writeInt(entry.sn);
                mplew.writeInt(entry.itemid);
                mplew.write(HexTool.hex("01 00 00 00 04 00 00 00 01 00 00 00 00 00 00 00"));
                mplew.writeInt(entry.price_old);
                mplew.writeLong(PacketHelper.getTime(-1L));
                mplew.writeLong(PacketHelper.getTime(-1L));
                mplew.writeLong(PacketHelper.getTime(-1L));
                mplew.writeLong(PacketHelper.getTime(-1L));
                mplew.writeInt(entry.price_new);
                mplew.writeInt(0);
                mplew.writeInt(entry.quantity);
                mplew.writeInt(entry.day);
                mplew.write(HexTool.hex("01 01 01 00 01"));
                mplew.writeInt(entry.gender);
                mplew.writeInt(entry.like);
                mplew.writeZeroBytes(16);
                if (entry.pack > 0) {
                    mplew.writeInt(entry.pack);
                    for (int i = 0; i < entry.pack; i++) {
                        mplew.writeInt(Integer.valueOf(entry.packhead[(i * 5)]).intValue());
                        mplew.writeInt(Integer.valueOf(entry.packhead[(i * 5 + 1)]).intValue());
                        mplew.writeInt(1);
                        mplew.writeInt(Integer.valueOf(entry.packhead[(i * 5 + 2)]).intValue());
                        mplew.writeInt(Integer.valueOf(entry.packhead[(i * 5 + 3)]).intValue());
                        mplew.writeInt(0);
                        mplew.writeInt(1);
                        mplew.writeInt(Integer.valueOf(entry.packhead[(i * 5 + 4)]).intValue());
                        mplew.writeInt(2);
                    }
                } else {
                    mplew.writeInt(0);
                }
            }
        }
        return mplew.getPacket();
    }
}