package net.sberg.elbook.tspcmpts;

public enum EnumAntragTyp {
	HBA("Heilberufeausweis (HBA)"),
	SMCB("Institutionskarte (SMC-B)");
	
    private String description;
	
	private EnumAntragTyp(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
