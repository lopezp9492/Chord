//package MusicMetadata;

import java.util.Comparator;

public class CatalogItem {
	public Release release;
	public Artist artist;
	public Song song;
	
	/*Comparator for sorting the list by ArtistName*/
    public static Comparator<CatalogItem> ArtistNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String ArtistName1 = s1.artist.name.toUpperCase();
	   String ArtistName2 = s2.artist.name.toUpperCase();

	   //ascending order
	   return ArtistName1.compareTo(ArtistName2);

	   //descending order
	   //return ArtistName2.compareTo(ArtistName1);
    }};
    
    
    
	/*Comparator for sorting the list by AlbumName*/
    public static Comparator<CatalogItem> AlbumNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String AlbumName1 = s1.release.name.toUpperCase();
	   String AlbumName2 = s2.release.name.toUpperCase();

	   //ascending order
	   return AlbumName1.compareTo(AlbumName2);

	   //descending order
	   //return AlbumName2.compareTo(AlbumName1);
    }};
    
	/*Comparator for sorting the list by SongName*/
    public static Comparator<CatalogItem> SongNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String SongName1 = s1.song.title.toUpperCase();
	   String SongName2 = s2.song.title.toUpperCase();

	   //ascending order
	   return SongName1.compareTo(SongName2);

	   //descending order
	   //return SongName2.compareTo(SongName1);
    }};
}
