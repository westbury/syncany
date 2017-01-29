/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2016 Philipp C. Heckel <philipp.heckel@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.crypto;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.syncany.config.to.EncryptedKey;
import org.syncany.util.StringUtil;

/**
 * Converter to properly encode a {@link SaltedSecretKey} when writing 
 * an XML. Salt and key are serialized as attributes.
 * 
 * @author Christian Roth <christian.roth@port17.de>
 */

public class SaltedSecretKeyConverter extends XmlAdapter<EncryptedKey, SaltedSecretKey>
{

	@Override
	public EncryptedKey marshal(SaltedSecretKey saltedSecretKey) throws Exception {
		EncryptedKey result = new EncryptedKey();
		result.setSalt(StringUtil.toHex(saltedSecretKey.getSalt()));
		result.setKey(StringUtil.toHex(saltedSecretKey.getEncoded()));
		return result;
	}

	@Override
	public SaltedSecretKey unmarshal(EncryptedKey v) throws Exception {
		byte[] saltBytes = StringUtil.fromHex(v.getSalt());
		byte[] keyBytes = StringUtil.fromHex(v.getKey());

		return new SaltedSecretKey(new SecretKeySpec(keyBytes, CipherParams.MASTER_KEY_DERIVATION_FUNCTION), saltBytes);
	}

}