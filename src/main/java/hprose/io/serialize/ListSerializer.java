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
 * ListSerializer.java                                    *
 *                                                        *
 * List serializer class for Java.                        *
 *                                                        *
 * LastModified: Apr 17, 2016                             *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/

package hprose.io.serialize;

import static hprose.io.HproseTags.TagClosebrace;
import static hprose.io.HproseTags.TagList;
import static hprose.io.HproseTags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

final class ListSerializer implements Serializer<List> {

    public final static ListSerializer instance = new ListSerializer();

    public final static void write(Writer writer, OutputStream stream, WriterRefer refer, List list) throws IOException {
        if (refer != null) refer.set(list);
        int count = list.size();
        stream.write(TagList);
        if (count > 0) {
            ValueWriter.writeInt(stream, count);
        }
        stream.write(TagOpenbrace);
        if (list instanceof RandomAccess) {
            for (int i = 0; i < count; ++i) {
                writer.serialize(list.get(i));
            }
        }
        else {
            for (Iterator i = list.iterator(); i.hasNext();) {
                writer.serialize(i.next());
            }
        }
        stream.write(TagClosebrace);
    }

    public final void write(Writer writer, List obj) throws IOException {
        OutputStream stream = writer.stream;
        WriterRefer refer = writer.refer;
        if (refer == null || !refer.write(stream, obj)) {
            write(writer, stream, refer, obj);
        }
    }
}
