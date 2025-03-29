// accountitem.model
export class AccountItem
{
    public id : number = -1;
    public name : string = '';
    public statementref : string = "";
    constructor();
    constructor(id : number, name: string);
    constructor(id? : number, name?: string)
    {
       this.id = id ?? -1;
       this.name = name ?? '';
    }
}
