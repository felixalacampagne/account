// epctransaction.model
export class EPCtransaction
{
    readonly iban : string = '';
    readonly name : string = '';
    readonly amount : string = '';
    readonly communication : string = '';

    constructor();
    constructor(iban: string, name: string, amount: string, communication: string);
    constructor(iban?: string, name?: string, amount?: string, communication?: string)
    {
       this.iban = iban ?? '';
       this.name = name ?? '';

       let amt: number = parseFloat(amount ?? '0.0');
       this.amount = amt.toFixed(2);
       this.communication = communication ?? '';
    }
}
