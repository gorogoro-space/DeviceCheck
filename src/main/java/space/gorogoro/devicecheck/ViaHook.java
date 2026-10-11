package space.gorogoro.devicecheck;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.UUID;

/**
 * ViaVersion のクラスを参照するのはこのクラスだけにしている。
 * ViaVersion が入っていないときにこのクラスを読み込まなければ、
 * 本体のプラグインはエラーにならずに動く。
 */
final class ViaHook {

    private ViaHook() {
    }

    /** クライアントのバージョン名（例: "1.21.4"）を返す */
    static String clientVersion(UUID uuid) {
        int protocol = Via.getAPI().getPlayerVersion(uuid);
        if (protocol < 0) {
            return "不明";
        }
        return ProtocolVersion.getProtocol(protocol).getName();
    }
}
