package com.googlecode.mp4parser.boxes.cenc;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CencDecryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Created by sannies on 27.09.2014.
 */
public class CencFileRoundtripTest {
    String baseDir = CencFileRoundtripTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    Map<UUID, SecretKey> keys;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation1;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation2;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation3;
    UUID uuidDefault;

    @Before
    public void setUp() throws Exception {
        uuidDefault = UUID.randomUUID();
        UUID uuidAlt = UUID.randomUUID();
        SecretKey cekDefault = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");
        SecretKey cekAlt = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");

        keys = new HashMap<UUID, SecretKey>();
        keys.put(uuidDefault, cekDefault);
        keys.put(uuidAlt, cekAlt);

        CencSampleEncryptionInformationGroupEntry cencNone = new CencSampleEncryptionInformationGroupEntry();
        cencNone.setEncrypted(false);

        CencSampleEncryptionInformationGroupEntry cencAlt = new CencSampleEncryptionInformationGroupEntry();
        cencAlt.setKid(uuidAlt);
        cencAlt.setIvSize(8);
        cencAlt.setEncrypted(true);

        CencSampleEncryptionInformationGroupEntry cencDefault = new CencSampleEncryptionInformationGroupEntry();
        cencAlt.setKid(uuidDefault);
        cencAlt.setIvSize(8);
        cencAlt.setEncrypted(true);
        keyRotation1 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation1.put(cencNone, new long[]{0, 1, 2, 3, 4});
        keyRotation1.put(cencAlt, new long[]{10, 11, 12, 13});

        keyRotation2 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation2.put(cencNone, new long[]{0, 2, 4, 6, 8});

        keyRotation3 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation3.put(cencDefault, new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15});

    }

    @Test
    public void testDefaultPlainFragMp4_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1", null);
    }

    @Test
    public void testDefaultPlainFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", null);
    }

    @Test
    public void testDefaultPlainStdMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1", null);
    }

    @Test
    public void testDefaultPlainStdMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", null);
    }

    @Test
    public void testSingleKeyMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, null, "cbc1", uuidDefault);
    }

    @Test
    public void testSingleKeyMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, null, "cenc", uuidDefault);
    }

    @Test
    public void testMultipleKeysStdMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1", uuidDefault);
    }


    @Test
    public void testMultipleKeysFragMp4_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1", uuidDefault);
    }

    @Test
    public void testMultipleKeysStdMp4_2_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1", uuidDefault);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1", uuidDefault);
    }

    @Test
    public void testMultipleKeysStdMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc", uuidDefault);
    }

    @Test
    public void testMultipleKeysFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc", uuidDefault);
    }

    @Test
    public void testMultipleKeysStdMp4_2_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", uuidDefault);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", uuidDefault);
    }

    public void testMultipleKeys(Mp4Builder builder, String testFile, Map<UUID, SecretKey> keys,
                                 HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation,
                                 String encAlgo, UUID uuidDefault) throws IOException {
        Movie m1 = MovieCreator.build(testFile);
        Movie m2 = new Movie();
        for (Track track : m1.getTracks()) {

            CencEncryptingTrackImpl cencEncryptingTrack = new CencEncryptingTrackImpl(track, uuidDefault, keys, keyRotation, encAlgo, false);
            m2.addTrack(cencEncryptingTrack);
        }
        Container c = builder.build(m2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));

        Movie m3 = MovieCreator.build(new MemoryDataSourceImpl(baos.toByteArray()));

        Movie m4 = new Movie();
        for (Track track : m3.getTracks()) {
            CencDecryptingTrackImpl cencDecryptingTrack =
                    new CencDecryptingTrackImpl((CencEncyprtedTrack) track, keys);
            m4.addTrack(cencDecryptingTrack);
        }
        Container c2 = builder.build(m4);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        c2.writeContainer(Channels.newChannel(baos2));
        Movie m5 = MovieCreator.build(new MemoryDataSourceImpl(baos2.toByteArray()));

        Iterator<Track> tracksPlainIter = m1.getTracks().iterator();
        Iterator<Track> roundTrippedTracksIter = m5.getTracks().iterator();

        while (tracksPlainIter.hasNext() && roundTrippedTracksIter.hasNext()) {
            verifySampleEquality(
                    tracksPlainIter.next().getSamples(),
                    roundTrippedTracksIter.next().getSamples());
        }

    }

    public void verifySampleEquality(List<Sample> orig, List<Sample> roundtripped) throws IOException {
        Iterator<Sample> origIter = orig.iterator();
        Iterator<Sample> roundTrippedIter = roundtripped.iterator();
        while (origIter.hasNext() && roundTrippedIter.hasNext()) {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            origIter.next().writeTo(Channels.newChannel(baos1));
            roundTrippedIter.next().writeTo(Channels.newChannel(baos2));
            Assert.assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());
        }

    }
}
