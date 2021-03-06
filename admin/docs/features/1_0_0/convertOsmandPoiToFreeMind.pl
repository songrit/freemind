use strict;
use DBI;
use Math::Trig;
require XML::Generator;
use utf8;
use HTML::Entities;

sub Encode {
    my $name = shift;
    my $xml = XML::Generator->new( ':pretty', escape => 'always,apos' );
    utf8::decode($name);
    my $encodedText = $xml->h1( $name );
    $encodedText = substr($encodedText, 4, length($encodedText)-9);
    $encodedText =~ s/"/'/g;
    HTML::Entities::encode_entities_numeric($encodedText);
    return $encodedText;
}

# Get filename from command line
# (1) quit unless we have the correct number of command-line args
my $num_args = $#ARGV + 1;
if ($num_args != 1) {
  print "\nUsage: convertOsmandPoiToFreeMind.pl file_name.odb\nWhere the odb files can be downloaded from http://code.google.com/p/osmand/downloads/list?num=700 and must be unzipped.\n";
  exit;
}

# (2) we got two command line args, so assume they are the
# first name and last name
my $file_name=$ARGV[0];
my $title = $file_name;
$title =~ s/.odb$//;
$title =~ s/.poi$//;
my $dbargs = {AutoCommit => 0, PrintError => 1};
my $dbh = DBI->connect("dbi:SQLite:dbname=${file_name}", "", "", $dbargs);

# Zeilen ausgeben
my $oldtype;
my $oldsubtype;
my ($id, $x, $y, $name, $type, $subtype, $site);
my $res = $dbh->selectall_arrayref("SELECT id, x, y, name, type, subtype, site FROM  poi order by type, subtype, name;");
print "<map version=\"1.0.0\"><node TEXT=\"${title}\">\n";
foreach my $row (@$res) {
  ($id, $x, $y, $name, $type, $subtype, $site) = @$row;
  next if "$name" eq "";
  if ($subtype eq "") {
      $subtype = "no_category";
  }
  # new type/subtype?
  if ($subtype ne $oldsubtype) {
      if (defined $oldsubtype) {
	  print "</node>\n";
      }
  }
  if ($type ne $oldtype) {
      if (defined $oldtype) {
	  print "</node>\n";
      }
      $oldtype = $type;
      $type = Encode($type);
      print "<node TEXT=\"$type\" FOLDED=\"true\">\n";
  }
  if ($subtype ne $oldsubtype) {
      $oldsubtype = $subtype;
      $subtype = Encode($subtype);
      print "<node TEXT=\"$subtype\" FOLDED=\"true\">\n";
  }
  my $encodedText = Encode($name);
  print "<node TEXT=\"$encodedText\" ID=\"$id\"";
  if($site ne "") {
      $site = Encode($site);
      print " LINK=\"$site\"";
  }
  print ">\n";
  my $lat;
  my $lon;
  my $n = 2 ** 31;
  $lon = $x / $n * 360.0 - 180.0;
  $lat = rad2deg(atan(sinh(pi * (1 - 2 * $y / $n))));
  print "<hook NAME=\"plugins/map/MapNodePositionHolder.properties\">\n<Parameters XML_STORAGE_MAP_LAT=\"$lat\" XML_STORAGE_MAP_LON=\"$lon\" XML_STORAGE_POS_LAT=\"$lat\" XML_STORAGE_MAP_TOOLTIP_LOCATION=\"false\" XML_STORAGE_POS_LON=\"$lon\" XML_STORAGE_TILE_SOURCE=\"org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource\$Mapnik\" XML_STORAGE_ZOOM=\"16\"/>\n</hook>\n";
  print "</node>\n";

}
if (defined $oldsubtype) {
    print "</node>\n";
}
if (defined $oldtype) {
    print "</node>\n";
}
print "</node></map>\n";

if ($dbh->err()) { die "$DBI::errstr\n"; }
$dbh->disconnect();
