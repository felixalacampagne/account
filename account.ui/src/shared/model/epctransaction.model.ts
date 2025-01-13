// epctransaction.model
export class EPCtransaction
{
    public iban : string = '';
    public name : string = '';
    public amount : string = '';
    public communication : string = '';

    constructor();
    constructor(iban: string, name: string, amount: string, communication: string);
    constructor(iban?: string, name?: string, amount?: string, communication?: string)
    {
       this.iban = iban ?? '';
       this.name = name ?? '';
       this.amount = amount ?? '';
       this.communication = communication ?? '';
    }
}
