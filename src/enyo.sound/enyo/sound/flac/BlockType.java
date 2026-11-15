package enyo.sound.flac;

import enyo.util.OpenEnum;

import java.lang.invoke.MethodHandles;

/**
 * The type of a metadata block.
 *
 * Block types are encoded using seven bits on the wire, so values are
 * represented as an {@linkplain OpenEnum open enumeration type}.
 *
 * @see MetadataInput
 */
public interface BlockType extends OpenEnum.OfInt {
    /**
     * The well-known block type constants.
     */
    enum E implements BlockType, OpenEnum.OfInt.Direct {
        /**
         * Block that describes the FLAC-encoded data in the file.
         */
        STREAMINFO,

        /**
         * Block of padding.
         */
        PADDING,

        /**
         * Block of application data.  It is preceded by a 32-bit
         * application identifier.
         */
        APPLICATION,

        /**
         * Block with data to help with seeking in the FLAC file.
         */
        SEEKTABLE,

        /**
         * Block of Ogg Vorbis comments.
         * @see <a href="https://www.xiph.org/vorbis/doc/v-comment.html">Ogg
         *   Vorbis I format specification: comment field and header
         *   specification</a>
         */
        VORBIS_COMMENT,

        /**
         * Block of CD-DA metadata.
         */
        CUESHEET,

        /**
         * Block of picture data.
         */
        PICTURE,
        ;
    }
    /**
     * Meta-type object for block types.
     */
    static final OpenEnum.OfInt.Type<BlockType> T = OpenEnum.intType(
        MethodHandles.lookup(), BlockType.class, E.class);
}
