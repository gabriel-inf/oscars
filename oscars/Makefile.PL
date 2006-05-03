use 5.8.6;
use ExtUtils::MakeMaker;
# See lib/ExtUtils/MakeMaker.pm for details of how to influence
# the contents of the Makefile that is written.
WriteMakefile(
    NAME              => 'OSCARS',
    VERSION           => '0.2',
    PREREQ_PM         => {}, # e.g., Module::Name => 1.1
);

package MY;

# What happens when we say 'make test' (from libwww)
sub test
{
    q(
TEST_VERBOSE=0

test: all
	$(FULLPERL) t/TEST $(TEST_VERBOSE)

);
}


