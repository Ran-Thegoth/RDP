package rs.cc.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rs.cc.ui.SessionActivity;

public class Barcode {

	private IfBranch _actions;
	private byte [] _bval = {};
	/**
	 * Создать баркод из последовательности байт
	 * @param val значение в виде последовательности байт
	 */
	public Barcode(byte [] val) {
		_bval = val;
	}
	/**
	 * Получить значение как строку
	 * @return значение как строка
	 */
	public String asString() {
		return new String(_bval);
	}
	/**
	 * Проверить на совпадение по регулярному выражению
	 * @param regex регулярное выражение
	 * @return true если совпадение найдено
	 */
	public boolean match(String regex)  {
		try {
			Pattern P = Pattern.compile(regex);
			Matcher m = P.matcher(asString());
			return m.find();
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Проверка пустой ли баркод
	 * @return true если баркод не содержит символов
	 */
	public boolean isEmpty() { return _bval.length == 0; }
	/**
	 * Присвоить баркоду последовательность байт
	 * @param b значение для присвоения
	 * @return баркод
	 */
	public  Barcode set(byte [] b) {
		if(b == null) b = new byte [] {};
		_bval = b;
		return this;
	}

	/**
	 * Присвоить баркоду строковое значение
	 * @param s значение
	 * @return баркод
	 */
	public  Barcode set(String s) {
		if(s == null) 
			_bval = new byte [] {};
		else
			_bval = s.getBytes();
		return this;
	}

	/**
	 * Содержит ли баркод последовательность байт 
	 * @param b последовательность для поиска
	 * @return true если последовательность найдена
	 */
	public boolean contains(byte [] b) {
		return indexOf(b) != -1;
	}
	/**
	 * Позиция в баркоде последовательности байт
	 * @param b последовательность для поиска
	 * @return -1 если последовательность не найдена иначе индекс первого вхождения 
	 */
	public int indexOf(byte [] b) {
		if(b == null || b.length == 0) return -1;
		if(b.length > _bval.length) return -1;
		for(int i =0;i<_bval.length-b.length;i++) {
			int j = 0;
			for(;j<b.length;j++)
				if(_bval[i+j] != b[j]) break;
			if(j == b.length) return i;
		}
		return -1;
	}
	/**
	 * Получить значение в виде последовательнояти байт
	 * @return последовательность байт
	 */
	public byte [] get() { return _bval; }
	/**
	 * Добавить к баркоду строковое значение
	 * @param s значение
	 * @return  баркод
	 */
	public Barcode append(String s) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(_bval);
			bos.write(s.getBytes());
			_bval = bos.toByteArray();
		} catch(IOException ioe) { }
		return this;
	}
	/**
	 * Очистить баркод (сделать пустым)
	 * @return баркод
	 */
	public Barcode clear() {
		_bval = new byte [] { };
		return this;
	}
	/**
	 * Заменить строковое значение в баркоде
	 * @param w что менять 
	 * @param s на что менять
	 * @return true если [b]w[/b] найдено и замена произведена
	 */
	public boolean replace(String w, String s) {
		if(w == null) return false;
		byte [] wa = w.getBytes();
		int idx = indexOf(wa);
		if(idx == -1) return false;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(_bval,0,idx);
			if(s != null)
				bos.write(s.getBytes());
			bos.write(_bval,idx+wa.length,_bval.length -wa.length-idx);
			_bval = bos.toByteArray();
			return true;
		} catch(IOException ioe) {
			return false;
		}
	}

	public int length() { return _bval.length; }
	public void before(SessionActivity a) {
		if(_actions != null)
			_actions.before(this, a);
	}
	public void after(SessionActivity a) { 
		if(_actions != null)
			_actions.after(this, a);
	}
	public void setActions(IfBranch a) {
		_actions = a;
	}

}
