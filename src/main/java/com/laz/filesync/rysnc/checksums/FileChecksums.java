package com.laz.filesync.rysnc.checksums;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import com.laz.filesync.rysnc.util.Constants;
import com.laz.filesync.rysnc.util.RsyncException;

/**
 * 文件对比
 *
 */
public class FileChecksums implements Serializable{
	private static final long serialVersionUID = 9065439598214380323L;
	private String name;
	private byte[] checksum;
	private List<BlockChecksums> blockChecksums = new ArrayList<BlockChecksums>();

	public FileChecksums(File file) {
		this(file,true);
	}
	
	public FileChecksums(File file,boolean blockCheck) {
		this.name = file.getName();
		this.checksum = generateFileDigest(file);
		if (blockCheck) {
			this.blockChecksums = generateBlockChecksums(file);
		}
	}

	private List<BlockChecksums> generateBlockChecksums(File file) {
		List<BlockChecksums> list = new ArrayList<BlockChecksums>();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buf = new byte[Constants.BLOCK_SIZE];
			int bytesRead = 0;
			long offset = 0;
			int index = 0;
			while ((bytesRead = fis.read(buf)) > 0) {
				list.add(new BlockChecksums(index ,buf, offset, bytesRead));
				offset += bytesRead;
				index ++;
			}
		} catch (FileNotFoundException e) {
			throw new RsyncException(e);
		} catch (IOException e) {
			throw new RsyncException(e);
		}finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	/**
	 * 获取整个文件的MD5
	 * @param file
	 * @return
	 */
	private byte[] generateFileDigest(File file) {
		FileInputStream fis = null;
		try {
			MessageDigest sha = MessageDigest.getInstance(Constants.MD5);
			fis = new FileInputStream(file);
			byte[] buf = new byte[Constants.BLOCK_SIZE];
			int read = 0;
			while ((read = fis.read(buf)) > 0) {
				sha.update(buf, 0, read);
			}
			return sha.digest();
		} catch (IOException e) {
			throw new RsyncException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RsyncException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RsyncException(e);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public List<BlockChecksums> getBlockChecksums() {
		return blockChecksums;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("fileChecksums for: ");
		builder.append(getName());
		builder.append("file checksum: ");
		builder.append(getHexChecksum());
		return builder.toString();
	}

	public String getHexChecksum() {
		return new String(Hex.encodeHex(getChecksum()));
	}

}
