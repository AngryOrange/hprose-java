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
 * DateTimeArraySerializer.java                           *
 *                                                        *
 * DateTime array serializer class for Java.              *
 *                                                        *
 * LastModified: Apr 17, 2016                             *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/

package hprose.io.serialize;

import static hprose.io.HproseTags.TagClosebrace;
import static hprose.io.HproseTags.TagList;
import static hprose.io.HproseTags.TagNull;
import static hprose.io.HproseTags.TagOpenbrace;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

final class DateTimeArraySerializer implements Serializer<Date[]> {

    public final static DateTimeArraySerializer instance = new DateTimeArraySerializer();

    public final static void write(OutputStream stream, WriterRefer refer, Date[] array) throws IOException {
        if (refer != null) refer.set(array);
        int length = array.length;
        stream.write(TagList);
        if (length > 0) {
            ValueWriter.writeInt(stream, length);
        }
        stream.write(TagOpenbrace);
        for (int i = 0; i < length; ++i) {
            Date e = array[i];
            if (e == null) {
                stream.write(TagNull);
            }
            else if (refer == null || !refer.write(stream, e)) {
                DateTimeSerializer.write(stream, refer, e);
            }
        }
        stream.write(TagClosebrace);
    }

    public final void write(Writer writer, Date[] obj) throws IOException {
        OutputStream stream = writer.stream;
        WriterRefer refer = writer.refer;
        if (refer == null || !refer.write(stream, obj)) {
            write(stream, refer, obj);
        }
    }
}
