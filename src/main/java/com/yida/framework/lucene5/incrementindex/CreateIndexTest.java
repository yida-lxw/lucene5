package com.yida.framework.lucene5.incrementindex;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.yida.framework.lucene5.util.LuceneUtils;

/**
 * 读取数据库表中数据创建索引
 * @author Lanxiaowei
 *
 */
public class CreateIndexTest {
	private PersonDao personDao;
	/**索引目录*/
	private String indexDir;
	
	public static void main(String[] args) throws IOException {
		String userIndexPath = "C:/zoieindex";
		PersonDao personDao = new PersonDaoImpl();
		//先读取数据库表中已有数据创建索引
		CreateIndexTest createIndexTest = new CreateIndexTest(personDao, userIndexPath);
		createIndexTest.index();
	}
	
	public CreateIndexTest(PersonDao personDao, String indexDir) {
		super();
		this.personDao = personDao;
		this.indexDir = indexDir;
	}

	public void index() throws IOException {
		List<Person> persons = personDao.findAll();
		if(null == persons || persons.size() == 0) {
			return;
		}
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		Analyzer analyzer = new AnsjAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
		for(Person person : persons) {
			Document document = new Document();
			document.add(new Field("id",person.getId().toString(),Field.Store.YES,
				Field.Index.NOT_ANALYZED,Field.TermVector.NO));
			document.add(new StringField("personName", person.getPersonName(), Field.Store.YES));
			document.add(new StringField("sex", person.getSex(), Field.Store.YES));
			document.add(new LongField("birth", person.getBirth().getTime(), Field.Store.YES));
			document.add(new TextField("nativePlace", person.getNativePlace(), Field.Store.YES));
			document.add(new StringField("job", person.getJob(), Field.Store.YES));
			document.add(new IntField("salary", person.getSalary(), Field.Store.YES));
			document.add(new StringField("hobby", person.getHobby(), Field.Store.YES));
			document.add(new StringField("deleteFlag", person.isDeleteFlag() + "", Field.Store.YES));
			//Zoie需要的UID[注意：这个域必须加，且必须是NumericDocValuesField类型，至于UID的域值是什么没关系，只要保证它是唯一的即可]
			document.add(new NumericDocValuesField("_ID", person.getId()));
			LuceneUtils.addIndex(writer, document);
		}
		writer.close();
		dir.close();
	}
}
