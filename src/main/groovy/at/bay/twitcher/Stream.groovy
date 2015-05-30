package at.bay.twitcher

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Created by pbayer.*/
@EqualsAndHashCode(includes = ['name','url'])
@ToString(excludes = ['url'],includePackage = false)
class Stream {

	String name
	String url
	String status
	String game
	String viewers

}
