/**********************************************************\
|                                                          |
|                          hprose                          |
|                                                          |
| Official WebSite: http://www.hprose.com/                 |
|                   http://www.hprose.org/                 |
|                                                          |
\**********************************************************/
/**********************************************************\
 *                                                        *
 * OtherTypeSerializer.java                               *
 *                                                        *
 * other type serializer class for Java.                  *
 *                                                        *
 * LastModified: May 19, 2016                             *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/

package hprose.io.serialize;

import hprose.io.HproseMode;
import static hprose.io.HproseTags.TagClass;
import static hprose.io.HproseTags.TagClosebrace;
import static hprose.io.HproseTags.TagObject;
import static hprose.io.HproseTags.TagOpenbrace;
import static hprose.io.HproseTags.TagString;
import hprose.io.accessor.Accessors;
import hprose.io.accessor.MemberAccessor;
import hprose.util.ClassUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class OtherTypeSerializer implements Serializer {

    public final static OtherTypeSerializer instance = new OtherTypeSerializer();

    private final static EnumMap<HproseMode, ConcurrentHashMap<Class<?>, SerializeCache>> memberCache = new EnumMap<HproseMode, ConcurrentHashMap<Class<?>, SerializeCache>>(HproseMode.class);

    static {
        memberCache.put(HproseMode.FieldMode, new ConcurrentHashMap<Class<?>, SerializeCache>());
        memberCache.put(HproseMode.PropertyMode, new ConcurrentHashMap<Class<?>, SerializeCache>());
        memberCache.put(HproseMode.MemberMode, new ConcurrentHashMap<Class<?>, SerializeCache>());
    }

    final static class SerializeCache {
        byte[] data;
        int refcount;
    }

    private static void writeObject(Writer writer, Object object, Class<?> type) throws IOException {
        Map<String, MemberAccessor> members = Accessors.getMembers(type, writer.mode);
        for (Map.Entry<String, MemberAccessor> entry : members.entrySet()) {
            MemberAccessor member = entry.getValue();
            member.serialize(writer, object);
        }
    }

    private static int writeClass(Writer writer, Class<?> type) throws IOException {
        SerializeCache cache = memberCache.get(writer.mode).get(type);
        if (cache == null) {
            cache = new SerializeCache();
            ByteArrayOutputStream cachestream = new ByteArrayOutputStream();
            Map<String, MemberAccessor> members = Accessors.getMembers(type, writer.mode);
            int count = members.size();
            cachestream.write(TagClass);
            ValueWriter.write(cachestream, ClassUtil.getClassAlias(type));
            if (count > 0) {
                ValueWriter.writeInt(cachestream, count);
            }
            cachestream.write(TagOpenbrace);
            for (Map.Entry<String, MemberAccessor> member : members.entrySet()) {
                cachestream.write(TagString);
                ValueWriter.write(cachestream, member.getKey());
                ++cache.refcount;
            }
            cachestream.write(TagClosebrace);
            cache.data = cachestream.toByteArray();
            memberCache.get(writer.mode).put(type, cache);
        }
        writer.stream.write(cache.data);
        if (writer.refer != null) {
            writer.refer.addCount(cache.refcount);
        }
        int cr = writer.lastclassref++;
        writer.classref.put(type, cr);
        return cr;
    }

    @SuppressWarnings({"unchecked"})
    public final static void write(Writer writer, OutputStream stream, WriterRefer refer, Object object) throws IOException {
        Class<?> type = object.getClass();
        Integer cr = writer.classref.get(type);
        if (cr == null) {
            cr = writeClass(writer, type);
        }
        if (refer != null) refer.set(object);
        stream.write(TagObject);
        ValueWriter.writeInt(stream, cr);
        stream.write(TagOpenbrace);
        writeObject(writer, object, type);
        stream.write(TagClosebrace);
    }

    public final void write(Writer writer, Object obj) throws IOException {
        OutputStream stream = writer.stream;
        WriterRefer refer = writer.refer;
        if (refer == null || !refer.write(stream, obj)) {
            write(writer, stream, refer, obj);
        }
    }
}
